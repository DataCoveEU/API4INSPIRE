var limit;
function plotFromAPI(property,dataURLtemp){
	dataTotal = 0;
	xData = [];
	yData = [];
	var dataURL = dataURLtemp.replace("%2527", "'").replace("%2527", "'");
	console.log("dataURL decode: "+dataURL);
	if(dataURL){
		$('#myModal3 .modal-body').empty();
		$('#myModal3 .modal-title').empty();
		//$('#myModal3 .modal-footer').empty(); 
		console.log("url First: " + dataURL);
		$.get(dataURL, function (res) {
			/*console.log(res);
			var data = res.value;
			var dataNext = res['@iot.nextLink'];
			console.log("dataNext: " + dataNext);

			dataTotal = res['@iot.count'];
			$('#myModal3 .modal-title').append(dataTotal + " observations for " + property);
			
			$.each(data, function (k, v) {
				//console.log(k,v);
				xData.push(v['resultTime']);
				yData.push(v['result']); 
			})*/
			
			var data = res.value;
			$.each(data, function (k, v) {
				//console.log(k,v);
				xData.push(v['resultTime']);
				yData.push(v['result']); 
			});
			var data = [{x: xData, y: yData, type: 'scatter'}];
			
			Plotly.newPlot('chartDiv', data);
			$("#myModal3").modal();
		})
		.done(function(){
			//var limit = 1000;
			/*if (dataTotal > limit) {
				page = 0;
				pages = parseInt(dataTotal) / parseInt(limit);
				console.log("NUMBER OF LOOPS:" + pages);
				var urlNext = '';
				while (page <= pages) {
					$('#loaderImage').show();
					page = page + 1;
					console.log("PAGE:" + page);
					urlNext = dataURL + '&$skip=' + page*limit;
					console.log("urlNext: " + urlNext);
					$.get(urlNext, function (res) {
						var data = res.value;
						dataNext = res['@iot.nextLink'];
						console.log(">>>Received NextLink" + dataNext);
						$.each(data, function (k, v) {
							//console.log(k,v);
							xData.push(v['resultTime']);
							yData.push(v['result']); 
						})
					})
					.done(function(){
						var data = [{x: xData, y: yData, type: 'scatter'}];
						Plotly.newPlot('chartDiv', data);
						$("#myModal3").modal();
					})
				}
			}*/
		})
	}
}
