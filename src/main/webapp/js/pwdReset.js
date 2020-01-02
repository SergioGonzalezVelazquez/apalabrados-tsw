function ResetPwdViewModel() {
    var token = null;
    var self = this;

    //View controllers
    this.showRestorePwd = ko.observable(false);
    this.showErrorMsg = ko.observable(false);
    this.showLoadingAnimation = ko.observable(true);
    this.showLoginRef = ko.observable(false);

    this.message = ko.observable();

    //Obtener el token
    const params = new URLSearchParams(document.location.search);
    token = params.get("code");
    if (!token) {
        self.showErrorMsg(true);
        self.showLoadingAnimation(false);
    } else {
        console.log("validar code:" + token)
        var info = {
            code: token
        };

        var data = {
            data: info,
            url: "validateToken",
            type: "post",
            success: tokenOk,
            error: tokenError
        };
        $.ajax(data);

    }

    //Form Fields
    this.pwd = ko.observable();
    this.pwd2 = ko.observable();

    //Button click
    this.resetPwdBtn = function () {
        if (self.pwd().length <= 0 || (self.pwd() !== self.pwd2())) {
            $("#message").attr("style", "color:red");
            self.message("Las contraseñas no coinciden");

        } else {
            var info = {
                code: token,
                pwd1: self.pwd(),
                pwd2: self.pwd2()
            };

            var data = {
                data: info,
                url: "updatePwd",
                type: "post",
                success: resetPwdOk,
                error: tokenError
            };
            $.ajax(data);
        }

    }

    function resetPwdOk(response) {
        $("#message").attr("style", "color:green");
        self.message("¡Contraseña actualizada!");
        self.showErrorMsg(false);
        self.showLoginRef(true);
        self.showRestorePwd(false);
    }


    function tokenOk(response) {
        self.showErrorMsg(false);
        self.showLoadingAnimation(false);
        self.showRestorePwd(true)
        self.showLoginRef(false);
    }

    function tokenError(response) {
        self.showErrorMsg(true);
        self.showLoadingAnimation(false);
        self.showRestorePwd(false);
        self.showLoginRef(true);
    }

    this.message = ko.observable();

}

var resetPwd = new ResetPwdViewModel();
ko.applyBindings(resetPwd);