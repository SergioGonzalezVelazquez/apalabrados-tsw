function UserViewModel() {
	var self = this;

	//View controllers
	this.showSignInForm = ko.observable(true);
	this.showSignUpForm = ko.observable(false);
	this.showRestorePwd = ko.observable(false);

	//Form Fields
	this.userEmail = ko.observable();
	this.userName = ko.observable();
	this.userPwd = ko.observable();
	this.userPwd2 = ko.observable();
	this.userRememberMe = ko.observable();

	this.message = ko.observable();

	//Controller view functions
	this.changeViewToSignUp = function () {
		this.showSignUpForm(true);
		this.showRestorePwd(false);
		this.showSignInForm(false);
	};

	this.changeViewToSignIn = function () {
		this.showSignUpForm(false);
		this.showRestorePwd(false);
		this.showSignInForm(true);
	};


	this.changeViewToRestorePwd = function () {
		this.showSignUpForm(false);
		this.showRestorePwd(true);
		this.showSignInForm(false);
	};

	//
	this.signInEmail = function () {
		var info = {
			email: this.userEmail(),
			pwd: this.userPwd(),
		};

		var data = {
			data: info,
			url: "login",
			type: "post",
			success: loginOk,
			error: error
		};
		$.ajax(data);
	}

	this.signUp = function () {
		var info = {
			email: this.userEmail(),
			userName: this.userName(),
			pwd1: this.userPwd(),
			pwd2: this.userPwd2(),
		};
		var data = {
			data: info,
			url: "signup",
			type: "post",
			success: singupOK,
			error: error
		};
		$.ajax(data);
	}

	this.updatePwd = function () {
		var info = {
			email: this.userEmail(),
		};
		var data = {
			data: info,
			url: "requestToken",
			type: "post",
			error: error
		};
		$.ajax(data);
		
		$("#message").attr("style", "color:green");
		self.message("Si hay un usuario registrado con el correo " + self.userEmail() +
			" recibirás un correo con las instrucciones para actualizar la contraseña");
	}


	function singupOK(response) {
		$("#message").attr("style", "color:green");
		self.message("Registrado correctamente. ¡Inicia sesión y juega!");
		self.changeViewToSignIn();
	}

	function loginOk(response) {
		console.log("login OK")
		console.log(response)
		var user = {
			"email": response.email,
			"userName": response.userName,
			"photo": response.photo
		}
		sessionStorage.setItem("user", JSON.stringify(user));
		
		window.location.href = "game.html";
	}

	function error(response) {
	
		$("#message").attr("style", "color:red; font-weight: bold;");
		if(response.responseJSON && response.responseJSON.message){
			self.message(response.responseJSON.message);
		}else{
			self.message("Error. No se pudo iniciar sesión")
		}

	}
}
// Inicialización de la api de google oauth
window.onload = function() {
	gapi.load('auth2', function() {
		  gapi.auth2.init({
		    client_id: '283528331959-6hurgg9as7kjcpa15b3itieeer0gap5f.apps.googleusercontent.com'
		  });
	 });
}

var user = new UserViewModel();
var photoURL;
ko.applyBindings(user);
//Metodo para recoger el login correcto con google, y guardar el correo en la bbdd
function onSignIn(googleUser) {

	var profile = googleUser.getBasicProfile();
	console.log("login OK");
	photoURL= profile.getImageUrl();
	
	var info = {
			id_token: googleUser.getAuthResponse().id_token
		};
		var data = {
			data: info,
			url: "loginWithGoogle",
			type: "post",
			success: singupOKGoogle
		};
	$.ajax(data);
	
}
function singupOKGoogle(response) {
	console.log("login OK")
	console.log(response)
	var user = {
		"email": response.email,
		"userName": response.userName,
		"photo": photoURL
	}
	sessionStorage.setItem("user", JSON.stringify(user));
	
	window.location.href = "game.html";
}

function logoutOKGoogle() {
	 
		 var auth2 = gapi.auth2.getAuthInstance();
			auth2.signOut().then(function () {
		    	console.log('User signed out Google.');
		    	sessionStorage.clear();
		    	window.location.href = "index.html";
		        console.log("Logout OK");
	    	});
}