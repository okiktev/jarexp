var lmodal=document.getElementById('linDownloadModal')
var lbtn=document.getElementById('linDownload')
lbtn.onclick=function(){lmodal.style.display='block'}

var wmodal=document.getElementById('winDownloadModal')
var wbtn=document.getElementById('winDownload')
wbtn.onclick=function(){wmodal.style.display='block'}

document.getElementsByClassName('close')[0].onclick=function(){
	wmodal.style.display='none'
}
document.getElementsByClassName('close')[1].onclick=function(){
	lmodal.style.display='none'
}
window.onclick=function(event){
  if(event.target==wmodal){
    wmodal.style.display='none'
  }
  if(event.target==lmodal){
	lmodal.style.display='none'
  }
}