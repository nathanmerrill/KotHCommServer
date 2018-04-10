+function(){
    function url(base, params){
        if (!params){
            return base;
        }

        const paramsEncoded = Object.keys(params)
            .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(params[key]))
            .join('&');
        return base + "?" + paramsEncoded;
    }
    function auth(write){
        const scope = write ? "write_access,no_expiry" : "";
        window.location = url("https://stackoverflow.com/oauth", {
            "client_id":window.oauth.clientId,
            "scope":scope,
            "redirect_uri": "https://koth.nmerrill.com/oauth/code"
        })
    }
    function login() {
        auth(false);
    }
}();
