function UserViewModel() {
	var self = this;

	var show

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
			success: signInEmailOk,
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
			success: signUpOk,
			error: error
		};
		$.ajax(data);
	}

	function signUpOk() {
		$("#message").attr("style", "color:blue");
		self.message("Register OK");
	}

	function signInEmailOk() {
		$("#message").attr("style", "color:blue");
		self.message("Login OK");
	}

	function error(response) {
		$("#message").attr("style", "color:red");
		self.message(response.responseText);
	}
}

var user = new UserViewModel();
ko.applyBindings(user);