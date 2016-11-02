/**
*  Javascript functions for the front end.
*        
*  Author: Patrice Lopez
*/

jQuery.fn.prettify = function () { this.html(prettyPrintOne(this.html(),'xml')); };

var grobid = (function($) {

    function defineBaseURL(ext) {
        var baseUrl = null;
        if ( $(location).attr('href').indexOf("index.html") != -1)
            baseUrl = $(location).attr('href').replace("index.html", ext);
        else 
            baseUrl = $(location).attr('href') + ext;
        return baseUrl;
    }

    function setBaseUrl(ext) {
        var baseUrl = defineBaseURL(ext);
        $('#gbdForm2').attr('action', baseUrl);
    }
    
    $(document).ready(function() {
        $("#subTitle").html("About");
        $("#divAbout").show();

        // for PDF based results  
        $("#divRestII").hide(); 
        
        $("#divDoc").hide();
        //$('#consolidateBlock').show();
        
        //createInputFile();
        createInputFile2();
        //createInputFile3();
        setBaseUrl('annotateJsonDocument');             

        $('#selectedService2').change(function() {
            processChange();
            return true;
        }); 

        $('#submitRequest2').bind('click', submitQuery2);
        
        $("#about").click(function() {
            $("#about").attr('class', 'section-active');
            $("#pdf").attr('class', 'section-not-active');
            $("#doc").attr('class', 'section-not-active');
            
            $("#subTitle").html("About"); 
            $("#subTitle").show();
            
            $("#divAbout").show();
            $("#divRestII").hide();
            $("#divDoc").hide();
            return false;
        });
        $("#doc").click(function() {
            $("#doc").attr('class', 'section-active');
            $("#pdf").attr('class', 'section-not-active');
            $("#about").attr('class', 'section-not-active');
            
            $("#subTitle").html("Doc"); 
            $("#subTitle").show();        
            
            $("#divDoc").show();
            $("#divAbout").hide();
            $("#divRestII").hide();
            return false;
        });
        $("#pdf").click(function() {
            $("#pdf").attr('class', 'section-active');
            $("#about").attr('class', 'section-not-active');
            $("#doc").attr('class', 'section-not-active');
            
            setBaseUrl('annotateJsonDocument');
            $("#subTitle").hide(); 
            processChange();
            
            $("#divDoc").hide();
            $("#divAbout").hide();
            $("#divRestII").show();
            return false;
        });
    });

    function ShowRequest2(formData, jqForm, options) {
        //var queryString = $.param(formData);
        $('#requestResult2').html('<font color="grey">Requesting server...</font>');
        return true;
    }

    function AjaxError2(jqXHR, textStatus, errorThrown) {
        $('#requestResult2').html("<font color='red'>Error encountered while requesting the server.<br/>"+jqXHR.responseText+"</font>");      
        responseJson = null;
    }
    
    function htmll(s) {
        return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }
    
    function SubmitSuccesful(responseText, statusText, xhr) {
        //var selected = $('#selectedService option:selected').attr('value');
        var display = "<pre class='prettyprint lang-xml' id='xmlCode'>";  
        var testStr = vkbeautify.xml(responseText);
        
        display += htmll(testStr);

        display += "</pre>";
        $('#requestResult').html(display);
        window.prettyPrint && prettyPrint();
        $('#requestResult').show();
    }

    function submitQuery2() {
        var selected = $('#selectedService2 option:selected').attr('value');
        if (selected == 'annotatePDFDocument') {
            // we will have a PDF back
            //PDFJS.disableWorker = true;
            
            var form = document.getElementById('gbdForm2');
            var formData = new FormData(form);
            var xhr = new XMLHttpRequest();
            var url = $('#gbdForm2').attr('action');
            xhr.responseType = 'arraybuffer';
            xhr.open('POST', url, true);
            ShowRequest2();
            xhr.onreadystatechange = function(e) {
                if (xhr.readyState == 4 && xhr.status == 200) {
                    var response = e.target.response;
                    var pdfAsArray = new Uint8Array(response);
                    // Use PDFJS to render a pdfDocument from pdf array
                    var frame = '<iframe id="pdfViewer" src="resources/pdf.js/web/viewer.html?file=" style="width: 100%; height: 1000px;"></iframe>';
                    $('#requestResult2').html(frame);
                    var pdfjsframe = document.getElementById('pdfViewer');
                    pdfjsframe.onload = function() { 
                        pdfjsframe.contentWindow.PDFViewerApplication.open(pdfAsArray); 
                    };
                } else  if (xhr.status != 200) {
                    AjaxError2(xhr);
                }
            };
            xhr.send(formData);  // multipart/form-data 
        } else {
            // we will have JSON annotations to be layered on the PDF
            
            // request for the annotation information
            var form = document.getElementById('gbdForm2');
            var formData = new FormData(form);
            var xhr = new XMLHttpRequest();
            var url = $('#gbdForm2').attr('action');
            xhr.responseType = 'json';
            xhr.open('POST', url, true);
            ShowRequest2();
            
            var nbPages = -1;

            // display the local PDF
            if (document.getElementById("input2").files[0].type == 'application/pdf') {
                var reader = new FileReader();
                reader.onloadend = function () {
                    // to avoid cross origin issue
                    //PDFJS.disableWorker = true;
                    var pdfAsArray = new Uint8Array(reader.result);
                    // Use PDFJS to render a pdfDocument from pdf array
                    PDFJS.getDocument(pdfAsArray).then(function (pdf) {
                        // Get div#container and cache it for later use
                        var container = document.getElementById("requestResult2");
                        // enable hyperlinks within PDF files.
                        //var pdfLinkService = new PDFJS.PDFLinkService();
                        //pdfLinkService.setDocument(pdf, null);

                        $('#requestResult2').html('');
                        nbPages = pdf.numPages;

                        // Loop from 1 to total_number_of_pages in PDF document
                        for (var i = 1; i <= nbPages; i++) {

                            // Get desired page
                            pdf.getPage(i).then(function(page) {

                                var div0 = document.createElement("div");
                                div0.setAttribute("style", "text-align: center; margin-top: 1cm;");
                                var pageInfo = document.createElement("p");
                                var t = document.createTextNode("page " + (page.pageIndex + 1) + "/" + (nbPages));
                                pageInfo.appendChild(t);
                                div0.appendChild(pageInfo);
                                container.appendChild(div0);

                                var scale = 1.5;
                                var viewport = page.getViewport(scale);
                                var div = document.createElement("div");

                                // Set id attribute with page-#{pdf_page_number} format
                                div.setAttribute("id", "page-" + (page.pageIndex + 1));

                                // This will keep positions of child elements as per our needs, and add a light border
                                div.setAttribute("style", "position: relative; border-style: solid; border-width: 1px; border-color: gray;");

                                // Append div within div#container
                                container.appendChild(div);

                                // Create a new Canvas element
                                var canvas = document.createElement("canvas");

                                // Append Canvas within div#page-#{pdf_page_number}
                                div.appendChild(canvas);

                                var context = canvas.getContext('2d');
                                canvas.height = viewport.height;
                                canvas.width = viewport.width;

                                var renderContext = {
                                    canvasContext: context,
                                    viewport: viewport
                                };

                                // Render PDF page
                                page.render(renderContext).then(function() {
                                    // Get text-fragments
                                    return page.getTextContent();
                                })
                                .then(function(textContent) {
                                    // Create div which will hold text-fragments
                                    var textLayerDiv = document.createElement("div");

                                    // Set it's class to textLayer which have required CSS styles
                                    textLayerDiv.setAttribute("class", "textLayer");

                                    // Append newly created div in `div#page-#{pdf_page_number}`
                                    div.appendChild(textLayerDiv);

                                    // Create new instance of TextLayerBuilder class
                                    var textLayer = new TextLayerBuilder({
                                      textLayerDiv: textLayerDiv, 
                                      pageIndex: page.pageIndex,
                                      viewport: viewport
                                    });

                                    // Set text-fragments
                                    textLayer.setTextContent(textContent);

                                    // Render text-fragments
                                    textLayer.render();

                                    //setupAnnotations(page, viewport, canvas, $('.annotationLayer'));
                                });
                            });
                        }
                    });
                }
                reader.readAsArrayBuffer(document.getElementById("input2").files[0]);
            }

            xhr.onreadystatechange = function(e) {
                if (xhr.readyState == 4 && xhr.status == 200) { 
                    var response = e.target.response;
                    //var response = JSON.parse(xhr.responseText);
                    //console.log(response);
                    setupAnnotations(response);
                } else  if (xhr.status != 200) {
                    AjaxError(xhr);
                }
            }
            xhr.send(formData);
        }
    }

    function setupAnnotations(response) {
        // we must check/wait that the corresponding PDF page is rendered at this point

        var json = response;
        var pageInfo = json.pages;
        
        var page_height = 0.0;
        var page_width = 0.0;

        var refBibs = json.refBibs;
        var mapRefBibs = {};
        if (refBibs) {
            for(var n in refBibs) {
                var annotation = refBibs[n];
                var theId = annotation.id;
                var theUrl = annotation.url;
                var pos = annotation.pos;
                if (pos) 
                    mapRefBibs[theId] = annotation;
                //for (var m in pos) {
                pos.forEach(function(thePos, m) {
                    //var thePos = pos[m];
                    // get page information for the annotation
                    var pageNumber = thePos.p;
                    if (pageInfo[pageNumber-1]) {
                        page_height = pageInfo[pageNumber-1].page_height;
                        page_width = pageInfo[pageNumber-1].page_width;
                    }
                    annotateBib(true, theId, thePos, theUrl, page_height, page_width, null);
                });
            }
        }

        // we need the above mapRefBibs structure to be created to perform the ref. markers analysis
        var refMarkers = json.refMarkers;
        if (refMarkers) {
            //for(var n in refMarkers) {
            refMarkers.forEach(function(annotation, n) {
                //var annotation = refMarkers[n];
                var theId = annotation.id;
                if (!theId)
                    return;
                // we take the first and last positions
                var targetBib = mapRefBibs[theId];
                if (targetBib) {
                    var theBibPos = {};
                    var pos = targetBib.pos;
                    //if (pos && (pos.length > 0)) {
                    var theFirstPos = pos[0];
                    var theLastPos = pos[pos.length-1];
                    theBibPos.p = theFirstPos.p;
                    theBibPos.w = Math.max(theFirstPos.w, theLastPos.w); 
                    theBibPos.h = Math.max(Math.abs(theLastPos.y - theFirstPos.y), theFirstPos.h) + Math.max(theFirstPos.h, theLastPos.h);
                    theBibPos.x = Math.min(theFirstPos.x, theLastPos.x); 
                    theBibPos.y = Math.min(theFirstPos.y, theLastPos.y); 
                    var pageNumber = theBibPos.p;
                    if (pageInfo[pageNumber-1]) {
                        page_height = pageInfo[pageNumber-1].page_height;
                        page_width = pageInfo[pageNumber-1].page_width;
                    }
                    annotateBib(false, theId, annotation, null, page_height, page_width, theBibPos);
                } else {
                    var pageNumber = annotation.p;
                    if (pageInfo[pageNumber-1]) {
                        page_height = pageInfo[pageNumber-1].page_height;
                        page_width = pageInfo[pageNumber-1].page_width;
                    }
                    annotateBib(false, theId, annotation, null, page_height, page_width, null);
                }
            });
        }
    }

    function annotateBib(bib, theId, thePos, url, page_height, page_width, theBibPos) {
        var page = thePos.p;
        var pageDiv = $('#page-'+page);
        var canvas = pageDiv.children('canvas').eq(0);;

        var canvasHeight = canvas.height();
        var canvasWidth = canvas.width();
        var scale_x = canvasHeight / page_height;
        var scale_y = canvasWidth / page_width;

        var x = thePos.x * scale_x;
        var y = thePos.y * scale_y;
        var width = thePos.w * scale_x;
        var height = thePos.h * scale_y;
        
//console.log('annotate: ' + page + " " + x + " " + y + " " + width + " " + height);
//console.log('location: ' + canvasHeight + " " + canvasWidth);
//console.log('location: ' + page_height + " " + page_width);
        //make clickable the area
        var element = document.createElement("a");
        var attributes = "display:block; width:"+width+"px; height:"+height+"px; position:absolute; top:"+y+"px; left:"+x+"px;";
        
        if (bib) {
            // this is a bibliographical reference
            // we draw a line
            if (url) {
                element.setAttribute("style", attributes + "border:2px; border-style:none none solid none; border-color: blue;");
                element.setAttribute("href", url);
                element.setAttribute("target", "_blank");
            }
            else
                element.setAttribute("style", attributes + "border:1px; border-style:none none dotted none; border-color: gray;");
            element.setAttribute("id", theId);
        } else {
            // this is a reference marker
            // we draw a box
            element.setAttribute("style", attributes + "border:1px solid; border-color: blue;");
            // the link here goes to the bibliographical reference
            if (theId) {
                element.onclick = function() {
                    goToByScroll(theId);
                };
            }
            // we need the area where the actual target bibliographical reference is
            if (theBibPos) {
                element.setAttribute("data-toggle", "popover");
                element.setAttribute("data-placement", "top");
                element.setAttribute("data-content", "content");
                element.setAttribute("data-trigger", "hover");
                var newWidth = theBibPos.w * scale_x;
                var newHeight = theBibPos.h * scale_y;
                var newImg = getImagePortion(theBibPos.p, newWidth, newHeight, theBibPos.x * scale_x, theBibPos.y * scale_y);
                $(element).popover({ 
                    content:  function () {
                        return '<img src=\"'+ newImg + '\" style=\"width:100%\" />';
                        //return '<img src=\"'+ newImg + '\" />';
                    }, 
                    html: true,
                    container: 'body'
                    //width: newWidth + 'px',
                    //height: newHeight + 'px'
//                  container: canvas,
                    //width: '600px',
                    //height: '100px'
                });
            }
        }   
        pageDiv.append(element);
    }

    /* jquery-based movement to an anchor, without modifying the displayed url and a bit smoother */
    function goToByScroll(id) {
        $('html,body').animate({scrollTop: $("#"+id).offset().top},'fast');
    }

    /* croping an area from a canvas */
    function getImagePortion(page, width, height, x, y) {
//console.log("page: " + page + ", width: " + width + ", height: " + height + ", x: " + x + ", y: " + y);
        // get the page div
        var pageDiv = $('#page-'+page);
//console.log(page);
        // get the source canvas
        var canvas = pageDiv.children('canvas')[0];
        // the destination canvas
        var tnCanvas = document.createElement('canvas');
        var tnCanvasContext = tnCanvas.getContext('2d');
        tnCanvas.width = width;
        tnCanvas.height = height;
        tnCanvasContext.drawImage(canvas, x , y, width, height, 0, 0, width, height);
        return tnCanvas.toDataURL();
    }
    
    function processChange() {
        var selected = $('#selectedService2 option:selected').attr('value');

        if (selected == 'annotatePDFDocument') {
            createInputFile2(selected);
            //$('#consolidateBlock2').show();
            setBaseUrl('annotatePDFDocument');
        }
        else if (selected == 'annotateJsonDocument') {
            createInputFile2(selected);
            //$('#consolidateBlock2').show();
            setBaseUrl('annotateJsonDocument');
        }
    }

    function createInputFile2(selected) {
        //$('#label').html('&nbsp;'); 
        $('#textInputDiv2').hide();
        $('#fileInputDiv2').show();
        
        $('#gbdForm2').attr('enctype', 'multipart/form-data');
        $('#gbdForm2').attr('method', 'post'); 
    }
        
})(jQuery);



