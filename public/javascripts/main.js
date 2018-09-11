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

    const showLanguageParams = () => {
        const select =  document.getElementById("language");
        const buildParameters = document.getElementById("build-parameters-field");
        showSelectInfo(select, [buildParameters]);
    };

    const showSelectInfo = (select, infos) => {
        if (select == null || infos == null){
            return;
        }
        const toggler = () => {
            const selected = select.value.replace(/ /g, "-").toLowerCase();
            for (const info of infos){
                show(info, info.classList.contains(selected));
            }
        };
        toggler();
        select.addEventListener('change', toggler)
    };

    const showGroupInfo = () => {
        const groups = document.getElementsByClassName("group");
        for (const group of groups) {

            showName(group, groups);
            showMatchmakerInfo(group);
            showMatchmakerParameters(group);
            showScorerInfo(group);
            showScorerParameters(group);
        }
    };

    const showName = (group, groups) => {
        const name = group.querySelector(".group-name");
        show(name, groups.length > 1);
    };

    const showMatchmakerInfo = (group) => {
        const matchmaker = group.querySelector(".matchmaker");
        const matchInfos = group.querySelectorAll(".match-info");
        showSelectInfo(matchmaker, matchInfos);
    };

    const showMatchmakerParameters = (group) => {
        const matchmaker = group.querySelector(".matchmaker");
        const parameters = group.querySelector(".matchmaker-parameters-field");
        showSelectInfo(matchmaker, [parameters]);
    };

    const showScorerInfo = (group) => {
        const scorer = group.querySelector(".scorer");
        const scorerInfos = group.querySelectorAll(".scorer-info");
        showSelectInfo(scorer, scorerInfos);
    };

    const showScorerParameters = (group) => {
        const scorer = group.querySelector(".scorer");
        const scorerParameters = group.querySelectorAll(".scorer-parameters");
        showSelectInfo(scorer, scorerParameters);
    };

    const show = (element, condition) => {
        element.style.display = condition ? "block" : "none";
    };

    const layoutCheckboxes = () => {
        const inputs = document.getElementsByTagName("dl");
        for (const input of inputs) {
            if (input.querySelector("input[type='checkbox']") != null){
                for (const child of input.querySelectorAll("*")){
                    child.style.display = "inline";
                }
            }
        }
    };

    const ready = () => {
        onClick('login', auth.bind(false));
        onClick('logout', deauth);
        showLanguageParams();
        showGroupInfo();
        layoutCheckboxes();
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", ready);
    } else {
        ready();
    }
}();
