+function () {
    const config = JSON.parse(document.getElementById('kothcomm-config').textContent);
    const url = (url, params) => {
        if (!params) {
            return url;
        }

        const paramsEncoded = Object.keys(params)
            .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(params[key]))
            .join('&');
        return url + "?" + paramsEncoded;
    };
    const auth = (write) => {
        const scope = write ? "write_access,no_expiry" : "";
        window.location.href = url("https://stackoverflow.com/oauth", {
            "client_id": config.oauthClient,
            "scope": scope,
            "redirect_uri": config.oauthRedirect,
            "state": window.location.href
        })
    };
    const login = auth.bind(false);
    const ready = () => {
        const loginElement = document.getElementById("login");
        if (loginElement != null){
            loginElement.addEventListener('click', login, false);
        }
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", ready);
    } else {
        ready();
    }
}();
