placeFooter = function () {

	//var body=document.body;
	//var html=document.documentElement;
	//var bodyH=Math.max(body.scrollHeight, body.offsetHeight, body.getBoundingClientRect().height, html.clientHeight, html.scrollHeight, html.offsetHeight); 

	var h1 = document.getElementsByTagName("h1")[0];
	var right=document.getElementById('right');
	var notfooter=document.getElementById('notfooter');
	var footer=document.getElementById('footer');
	var about=document.getElementById('about');
	var lang=document.getElementById('lang');
	var slides=document.getElementById('slides');
	var logo=document.getElementById('logo');

	//console.log('notfooter ' + notfooter.offsetHeight + ' : right ' + right.offsetHeight);

	var nfHight = logo.offsetHeight + 30 + about.offsetHeight + lang.offsetHeight + h1.offsetHeight;

	if (nfHight + footer.offsetHeight + 20 < right.offsetHeight) {
		footer.style.display = 'block';
		notfooter.style.height = (right.offsetHeight - footer.offsetHeight) + 'px';
	} else {
		footer.style.display = 'none';
		notfooter.style.height = right.offsetHeight;
	}
	
	
	var downloads=document.getElementById('downloads');
	
	if (downloads.offsetHeight + 40 + slides.offsetHeight > right.offsetHeight) {
		slides.style.height = right.offsetHeight - downloads.offsetHeight - 40;
	} else {
		slides.style.height = 495;
	}
}

window.onload = placeFooter

window.onresize = placeFooter