+function(){
    const baseUrl = window.location.protocol + "//" + window.location.hostname;
    function url(params){
        if (!params){
            return baseUrl;
        }

        const paramsEncoded = Object.keys(params)
            .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(params[key]))
            .join('&');
        return baseUrl + "?" + paramsEncoded;
    }
    function auth(write){
        const scope = write ? "write_access,no_expiry" : "";
        window.location = url("https://stackoverflow.com/oauth", {
            "client_id":window.kothcomm.oauthClient,
            "scope":scope,
            "redirect_uri": baseUrl+"/oauth/code",
            "state": window.location.href
        })
    }
    function login() {
        auth(false);
    }
}();
