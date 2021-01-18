
$(document).ready(function() {

	$('#table1').DataTable({
		columns: [
		   		{data: "id", title: "Sample"},
				{data: "pos", title: "Position"},
		   		{data: "var", title: "Variant"},
		   		{data: "ref", title: "Reference"},
		   		{data: "level", title: "Level"},
		   		{data: "type", title: "Type"},
		   		{data: "filter", title: "Filter"}
		],
		data: {{json(variants)}}
	})

 });
