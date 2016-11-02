# grobid-istex

Work in progress.

This [GROBID](https://github.com/kermitt2/grobid) module enriches PDF with clickable areas corresponding to the bibliographical references also present in [ISTEX](http://www.istex.fr). ISTEX permits the access to more than 17 million scientific documents from the major publishers to all French Research and Teaching organisations. 

The overall process is the following one: 

1. grobid-core analyses the source PDF and extracts the structured bibliographical references together with the corresponding PDF areas. 

2. for each extracted bibliographical reference, the ISTEX OpenURL service is used to check if the corresponding full text is present or not in the ISTEX resources and retrieves the identifier of the full text PDF. 

Optionally, before the call to the ISTEX OpenURL service, a call to CrossRef is done to retrieve a DOI and richer metadata for the recognized bibliographical reference.These additional information will improve the resolution rate of the OpenURL service.

3. for the bibliographical references present in ISTEX, we can produce: 

(i) a PDF enriched with a clickable areas added to the source PDF covering the text of the bibliographical reference. Clicking on this area will call the browser and leads directly to the cited PDF in the ISTEX holding (external URI PDF link action, access to the ISTEX PDF must be authorized for the user).  

(ii) a set of annotations in JSON format including coordinates in the source PDF in order to create an interactive additional layer on top of the PDF rendered in the browser, 

4. the reference markers present in the document body are also made clickable and are linked to the text of the corresponding bibliographical reference ("GoTo" PDF action). 

## Install, build, run

First install the latest development version of GROBID as explained by the [documentation](http://grobid.readthedocs.org).

Copy the present module ```grobid-istex``` as sibling sub-project to grobid-core, grobid-trainer, etc.:
> cp -r grobid-istex grobid/

Build GROBID with:
> cd PATH-TO-GROBID/grobid/

> mvn -Dmaven.test.skip=true clean install

Build grobid-istex module:
> cd PATH-TO-GROBID/grobid/grobid-istex

> mvn clean install


## Start the service

The service consumes a PDF and enriches it with clickable bibliographical references corresponding to the available ISTEX resources (service ```/annotatePDFDocument```) or return a set of JSON annotations related to the bibliographical information (service ```/annotateJsonDocument```). The service exploits multithreading. 

To start the service:

> mvn jetty:run-war

Demo/console web app is then accessible by default at ```http://localhost:8080```.

## Batch process PDF files

Apply the PDF enrichment to all the PDF files in a repository. 

> java -jar target/grobid-istex-0.4.1-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties -dIn ~/test/ -dOut ~/test/ -exe annotateCitations

For better performance and production, it is advised to use the above service which is using multithreading, and not the batch mode.

## Example

Given an orignal PDF such as this [one](doc/d-67-00463.pdf), the grobid-istex module can produced the [following](d-67-00463.grobid.pdf) enriched version, with clickable links to the available cited ISTEX resources. Note that this PDF is an _Open Access_ article available on PubMed Central allowing reuse. 

## Licence

As [GROBID](https://github.com/kermitt2/grobid), distributed under [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0). 
