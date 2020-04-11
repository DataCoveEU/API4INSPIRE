function tableFromAPI(property,dataURLtemp){
	var dataURL = dataURLtemp.replace("%2527", "'").replace("%2527", "'");
	if(dataURL){
		$('#myModal2 .modal-body').empty();
		$('#myModal2 .modal-title').empty();
		$('#myModal2 .modal-footer').empty(); 
		$.get(dataURL, function (res) {
			var data = res.value;
			var dataNext = res['@iot.nextLink'];
			var dataTotal = res['@iot.count'];
			$('#myModal2 .modal-title').append(dataTotal + " observations for " + property);
			var modalBody = '<table class="table table-striped"><tr><th>Time</th><th>Value</th></tr>';
			$.each(data, function (k, v) {
				console.log(k,v);
				modalBody += '<tr><td>'+v['resultTime']+'</td><td>'+v['result']+'</td></tr>'; 
			})
			modalBody += '</table>';
			$('#myModal2 .modal-body').append(modalBody);
			if(dataNext){
				$('#myModal2 .modal-footer').append('<button class="btn btn-default" onclick="tableFromAPI(\'' + property + '\',\'' + dataNext + '\');">Next</button>')
			}			
			$("#myModal2").modal();
		})
	}
}
