const hiddenFileInput = document.querySelector(".hidden_file_input");
const fileChooserButton = document.querySelector(".file_chooser_btn");
const fileDropZoneArea = document.querySelector(".file_drop_area");

fileChooserButton.addEventListener("click", function() {
  hiddenFileInput.click();
  hiddenFileInput.addEventListener("change", function() {
    const chosenFiles = hiddenFileInput.files;
    uploadFiles(chosenFiles);
  });
});

fileDropZoneArea.addEventListener("dragover", function(e) {
  e.preventDefault();
});

fileDropZoneArea.addEventListener("drop", function(e) {
  e.preventDefault();
  const chosenFiles = e.dataTransfer.files;
  uploadFiles(chosenFiles);
});

function uploadFiles(files) {
  for (file of files) {
    uploadFile(file);
  }
}

function uploadFile(file) {
  const xhr = new XMLHttpRequest();
  xhr.open("POST", "/uploads");
  xhr.setRequestHeader("FileName",file.name);
  xhr.send(file);
}
