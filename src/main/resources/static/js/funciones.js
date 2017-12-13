$(document).ready( function(){
	console.log("ESTA LISTA LA PAGINA!!!");
	
	var client = filestack.init('AacZnjR5xQgK8Wjc9IRSJz');
	
	$('#botonSubirImagen').click( function(){
		client.pick({fromSources:["local_file_system","url","googledrive","dropbox","onedrive"],
		      accept:[".srt",".sub",".ssa",".ass",".txt"],
		      maxSize:2097152,
		      maxFiles:1,
		      minFiles:1,
		      lang:"es"}).then(function(result) {
			$('#inputUrlImagen').val(result.filesUploaded[0].url);
		});
	});
	
	
	
	
} );

console.log("CARGO PERO AUN NO ESTA LISTA LA PAGINA");
