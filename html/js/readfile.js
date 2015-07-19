
function fileSelect(evt) {
  alert("hep");
    if (window.File && window.FileReader && window.FileList && window.Blob) {
        var files = evt.target.files;

        var result = '';
        var file;
        for (var i = 0; file = files[i]; i++) {

            // if the file is not an image, continue
        //    if (!file.type.match('image.*')) {
        //        continue;
         //   }
      
            reader = new FileReader();
            reader.onload = (function (tFile) {
                return function (evt) {
                    pdfdata = evt.target.result;
                    var div = document.createElement('div');
                    processPdf(evt.target.result);
//                    div.innerHTML = '<img style="width: 90px;" src="' + evt.target.result + '" />';
  //                  document.getElementById('filesInfo').appendChild(div);
                };
            }(file));
            reader.readAsDataURL(file);
        }
    } else {
        alert('The File APIs are not fully supported in this browser.');
    }
}
var content = [];

function processPdf(data) {
  PDFJS.getDocument(data).then(function(pdf) {
    pdfObject = pdf;
    // you can now use *pdf* here
    for (var i = 1; i <= pdf.numPages; i++) {
      pdf.getPage(i).then(function(page) {
        pageObject = page;
        
        page.getTextContent().then(function (tc) {
          content.push((tc.items || tc).map(function (x) { return x.str; }).join(' '));
        }); 
        // you can now use *page* here
        var scale = 1.5;
        var viewport = page.getViewport(scale);
        
        var canvas = document.getElementById('the-canvas');
        
        var context = canvas.getContext('2d');
        canvas.height = viewport.height;
        canvas.width = viewport.width;
        
        var renderContext = {
          canvasContext: context,
          viewport: viewport
        };
        page.render(renderContext);
      });
    }
    
  });

}


document.getElementById('filesToUpload').addEventListener('change', fileSelect, false);

function dragOver(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    evt.dataTransfer.dropEffect = 'copy';
}

var dropTarget = document.getElementById('dropTarget');
dropTarget.addEventListener('dragover', dragOver, false);
dropTarget.addEventListener('drop', fileSelect, false);
