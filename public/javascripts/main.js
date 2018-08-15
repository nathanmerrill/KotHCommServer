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
        });
    };

    const deauth = () => {
        window.location.href = config.deauthUrl;
    };

    const onClick = (id, func) => {
        const element = document.getElementById(id);
        if (element != null) {
            element.addEventListener('click', func, false)
        }
    };

    const toggleLanguageParams = () => {
        const select =  document.getElementById("language");
        const buildParameters = document.getElementById("buildParameters_field");
        if (select == null || buildParameters == null){
            return;
        }
        const toggler = () => {
            const selected = select.options[select.selectedIndex].text;
            if (selected === "Java" || selected === "Python 2" || selected === "Python 3") {
                buildParameters.style.display = "block";
            } else {
                buildParameters.style.display = "none";
            }
        };
        toggler();
        select.addEventListener('change', toggler)
    };

    const ready = () => {
        onClick('login', auth.bind(false));
        onClick('logout', deauth);
        toggleLanguageParams();
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", ready);
    } else {
        ready();
    }
}();
