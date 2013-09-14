<%-- 
    Document   : index
    Created on : May 31, 2013, 4:29:34 PM
    Author     : anthony
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<html>
	<head>
		<title>Superman</title>
		<script type="text/javascript">
			function getQueryString(formname) {
				var form = document.forms[formname];
				var qstr = "";

				function GetElemValue(name, value) {
					qstr += (qstr.length > 0 ? "&" : "")
						+ escape(name).replace(/\+/g, "%2B") + "="
						+ escape(value ? value : "").replace(/\+/g, "%2B");
						//+ escape(value ? value : "").replace(/\n/g, "%0D");
				}

				var elemArray = form.elements;
				for (var i = 0; i < elemArray.length; i++) {
					var element = elemArray[i];
					var elemType = element.type.toUpperCase();
					var elemName = element.name;
					if (elemName) {
						if (elemType == "TEXT"
								|| elemType == "TEXTAREA"
								|| elemType == "PASSWORD"
								|| elemType == "BUTTON"
								|| elemType == "RESET"
								|| elemType == "SUBMIT"
								|| elemType == "FILE"
								|| elemType == "IMAGE"
								|| elemType == "HIDDEN")
							GetElemValue(elemName, element.value);
						else if (elemType == "CHECKBOX" && element.checked)
							GetElemValue(elemName, 
								element.value ? element.value : "On");
						else if (elemType == "RADIO" && element.checked)
							GetElemValue(elemName, element.value);
						else if (elemType.indexOf("SELECT") != -1)
							for (var j = 0; j < element.options.length; j++) {
								var option = element.options[j];
								if (option.selected)
									GetElemValue(elemName,
										option.value ? option.value : option.text);
							}
					}
				}
				return qstr;
			}
		</script>
		<script type="text/javascript">
			function submitQuery(){
				
				var query = document.getElementById("query_field").value;
				var req = new XMLHttpRequest();
				req.open("GET", "./NewQuery?query=" + escape(query), true);
				req.onreadystatechange = function () {

					if (req.readyState === 4) {
						renderInputForm(req);
					}
				}
				var cont = document.getElementById("form_container");
				cont.innerHTML += "<br />Please Wait..."
				req.send(null);
			}
			function renderInputForm(req){
				document.getElementById("form_container").innerHTML = 
						'<div style="display:inline-block;text-align:left;borde-style:solid;border-weight:2px;border-radius:6px;">' +
						req.responseText + '</div>'
			}
			function submitInput(){
				
				var req = new XMLHttpRequest();
				req.open("GET", "./SubmitInput?" + getQueryString("input_form"), true);
				req.onreadystatechange = function () {

					if (req.readyState === 4 ) {
						window.open("map.html", "_self");
					}
				}
				var cont = document.getElementById("form_container");
				cont.innerHTML += "<br />Please Wait..."
				req.send(null);
			}
		</script>
	</head>
	<body style="width:960px; margin: 0 auto; background-color:lightgrey; font-family:arial,helvetica,clean,sans-serif;font-size:150%;background-image: radial-gradient(circle farthest-corner at center, #FFFFFF 0%, #F3FDFF 100%);">
		<div style="top:120px;width:720px; position:relative;margin: 0 auto; box-shadow: 0px 0px 16px grey;border-radius:6px;text-align:center;background-image: radial-gradient(circle farthest-corner at center, #FFFFFF 0%, #F0F0F0 100%);">
			<div id="form_container" style="margin: 0 auto; padding-top:120px; padding-bottom:120px;display:inline-block">
				<form id="query_input" onsubmit="submitQuery()" action="javascript:void(0);">
					<span style="font-size:150%">SMART Search<br /></span>
						find
						<input type="text" id="query_field" style="width:400px" />
						<input type="submit" />
				</form>
			</div>
		</form></div>
	</body>
</html>