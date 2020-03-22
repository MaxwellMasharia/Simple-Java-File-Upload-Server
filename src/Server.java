import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.UUID;

public class Server {
    private static final String ROOT = "src/web/index.html";
    private static final String ROOT_CSS = "src/web/style.css";
    private static final String ROOT_JS = "src/web/app.js";

    private static int getFileSize(String fileName) {
        return (int) new File(fileName).length();
    }

    private static byte[] getFileData(String fileName) throws IOException {
        byte[] fileData = new byte[getFileSize(fileName)];
        FileInputStream fileInputStream = new FileInputStream(fileName);
        int readBytes = fileInputStream.read(fileData);
        System.out.println("Read " + readBytes + " bytes from the file " + fileName);
        fileInputStream.close();
        return fileData;
    }

    private static String getFileType(String fileName) {
        if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".js")) {
            return "text/javascript";
        }
        return "";
    }

    private static void sendRequestedFile(HttpExchange httpExchange, String fileName) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", getFileType(fileName));
        httpExchange.sendResponseHeaders(200, getFileSize(fileName));

        OutputStream dataOut = httpExchange.getResponseBody();
        dataOut.write(getFileData(fileName));
        dataOut.flush();
        dataOut.close();
    }

    private static byte[] getByteArray(byte[] data, int length) {
        byte[] result = new byte[length];
        System.arraycopy(data, 0, result, 0, length);
        return result;
    }

    private static void uploadFile(HttpExchange httpExchange) {
        // Get the fileName
        String randomString = UUID.randomUUID().toString();
        String fileName = randomString + httpExchange.getRequestHeaders().get("fileName").get(0).trim();
        File file = new File("src/uploads/" + fileName);
        try {
            boolean b = file.createNewFile();
            if (b) System.out.println("Created the file " + file.getAbsolutePath());
            InputStream dataIn = httpExchange.getRequestBody();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(dataIn);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));

            while (true) {
                byte[] buffer = new byte[8192];
                int readBytes = bufferedInputStream.read(buffer);
                if (readBytes != buffer.length) {
                    bufferedOutputStream.write(getByteArray(buffer, readBytes));
                    bufferedOutputStream.flush();
                    break;
                }
                bufferedOutputStream.write(buffer);
                bufferedOutputStream.flush();
            }

            httpExchange.sendResponseHeaders(200, 0);
            bufferedInputStream.close();
            bufferedOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(4040), 0);
        server.createContext("/", httpExchange -> sendRequestedFile(httpExchange, ROOT));
        server.createContext("/app.js", httpExchange -> sendRequestedFile(httpExchange, ROOT_JS));
        server.createContext("/style.css", httpExchange -> sendRequestedFile(httpExchange, ROOT_CSS));
        server.createContext("/uploads", Server::uploadFile);


        server.start();
        System.out.println("Server started ... Listening @port " + server.getAddress().getPort());
    }
}
