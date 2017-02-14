window.onload = function () {

	var body=document.body;
	var html=document.documentElement;
	var bodyH=Math.max(body.scrollHeight, body.offsetHeight, body.getBoundingClientRect().height, html.clientHeight, html.scrollHeight, html.offsetHeight); 

	var header=document.getElementById('header');
	var menu=document.getElementById('menu');
	var content=document.getElementById('content');
	var footer=document.getElementById('footer');

	if (bodyH > header.offsetHeight + menu.offsetHeight + content.offsetHeight) {
		var dif = bodyH - (header.offsetHeight + menu.offsetHeight + content.offsetHeight + footer.offsetHeight);
		content.style.height = (content.offsetHeight + dif) + 'px';
	}
}