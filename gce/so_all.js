'use strict';
window.SE = function (v) {
    function n(d, k) {
        var e = d[k];
        if (!e) throw k + " required";
        return e
    }

    function w(d, k, e, h) {
        for (var a, g = "sec" + p++, c = x; window[g] || f.getElementById(g);) g = "sec" + p++;
        window[g] = function (c) {
            window[g] = v;
            a.parentNode.removeChild(a);
            c.error_id ? h({
                errorName: c.error_name,
                errorMessage: c.error_message
            }) : e({
                accessToken: d,
                expirationDate: k,
                networkUsers: c.items
            })
        };
        c += "?pagesize=100&access_token=" + l(d) + "&key=" + l(s) + "&callback=" + l(g);
        a = f.createElement("script");
        a.type = "text/javascript";
        a.src =
            c;
        a.id = g;
        f.getElementsByTagName("head")[0].appendChild(a)
    }
    var t, u, m, s, y = window.navigator.userAgent,
        f = window.document,
        l = window.encodeURIComponent,
        p = 1,
        z = "sew" + p++,
        x = "https://api.stackexchange.com/2.0/me/associated";
    return {
        authenticate: function (d) {
            if (!d) throw "must pass an object";
            var k, e, h, a, g = n(d, "success"),
                c = d.scope,
                f = "",
                m = p++,
                q = u + "&client_id=" + t + "&state=" + m,
                r = d.error;
            if (c && "[object Array]" !== Object.prototype.toString.call(c)) throw "scope must be an Array";
            c && (f = c.join(" "));
            0 < f.length && (q += "&scope=" +
                l(f));
            h = function (b) {
                if ("https://stackexchange.com" === b.origin && b.source === e) {
                    var a, c, f = b.data.substring(1).split("&");
                    b = {};
                    for (a = 0; a < f.length; a++) c = f[a].split("="), b[c[0]] = c[1]; + b.state === m && (k && window.removeEventListener("message", h), e.close(), (a = b.access_token) ? ((b = b.expires) && (b = new Date((new Date).getTime() + 1E3 * b)), d.networkUsers ? w(a, b, g, r) : g({
                        accessToken: a,
                        expirationDate: b
                    })) : r && r({
                        errorName: b.error,
                        errorMessage: b.error_description
                    }))
                }
            };
            !window.postMessage || !window.addEventListener || 9 >= (/MSIE (\d+\.\d+)/.exec(y) || [])[1] ? a = setInterval(function () {
                if (e)
                    if (e.closed) clearInterval(a);
                    else {
                        var b = e.frames["se-api-frame"];
                        b && (clearInterval(a), h({
                            origin: "https://stackexchange.com",
                            source: e,
                            data: b.location.hash
                        }))
                    }
            }, 50) : (k = !0, window.addEventListener("message", h));
            e = window.open(q, z, "width=660,height=480")
        },
        init: function (d) {
            if (!d) throw "must pass an object";
            var f = n(d, "clientId"),
                e = n(d, "channelUrl"),
                h = n(d, "complete"),
                a = window.location.protocol,
                g = a.substring(0, a.length - 1),
                a = (a + "//" + window.location.host).toLowerCase();
            s = n(d,
                "key");
            t = f;
            m = e;
            //if (0 !== m.toLowerCase().indexOf(a)) throw "channelUrl must be under the current domain";
            u = "https://stackexchange.com/oauth/dialog?redirect_uri=" + l("https://stackexchange.com/oauth/login_success?assisted=" + f + "&protocol=" + g + "&proxy=" + l(m));
            setTimeout(function () {
                h({
                    version: "10790"
                })
            })
        }
    }
}();