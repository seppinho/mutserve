
$(document).ready(function() {

	$('#table1').DataTable({
		columns: [
		   		{data: "id", title: "Sample"},
		   		{data: "filter", title: "Filter"},
		   		{data: "pos", title: "Position"},
		   		{data: "var", title: "Variant"},
		   		{data: "ref", title: "Reference"}
		],
		data: {{json(variants)}}
	})

 });
