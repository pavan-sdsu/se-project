$(document).ready(() => {
	let role = localStorage.getItem("role");
	if(role != "Manager") $("#exp-occ-item, #exp-inc-item, #inc-rep-item, #base-rate-item").addClass("d-none");
})