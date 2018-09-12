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
        const multipleGroups = groups.length > 1;
        for (let i = 0; i < groups.length; i++) {
            const group = groups[i];
            showName(group, multipleGroups);
            showMatchmakerInfo(group);
            showMatchmakerParameters(group);
            showScorerInfo(group);
            showScorerParameters(group);
            showRemoveButton(group, multipleGroups);
            reindexElements(group, i);
        }
    };

    const reindexElements = (group, i) => {
        const index = i + "";
        group.setAttribute("id", "group"+index);
        const attributes = ['id', 'for', 'name'];
        for (const attribute of attributes) {
            const elements = group.querySelectorAll("*["+attribute+"^='group']");
            for (const element of elements) {
                const parts = element.getAttribute(attribute).split(/\d+/g);
                element.setAttribute(attribute, parts.join(index))
            }
        }
    };

    const showName = (group, multipleGroups) => {
        const name = group.querySelector(".group-name");
        show(name, multipleGroups);
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

    const showRemoveButton = (group, multipleGroups) => {
        const removeButton = group.querySelector(".remove-group");
        removeButton.addEventListener('click', () => {
            if (group.parentNode) {
                group.parentNode.removeChild(group);
            }
            showGroupInfo();
        });
        show(removeButton, multipleGroups);
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

    const addGroup = () => {
        const groups = document.getElementsByClassName("group");
        const group = groups[groups.length - 1];
        let row = group.parentNode;
        if (row.childElementCount === 2){
            const clone = row.cloneNode(false);
            row.parentNode.appendChild(clone);
            row = clone
        }
        row.appendChild(group.cloneNode(true));
        showGroupInfo();
    };

    const ready = () => {
        onClick('login', auth.bind(false));
        onClick('logout', deauth);
        onClick('add-group', addGroup);
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
