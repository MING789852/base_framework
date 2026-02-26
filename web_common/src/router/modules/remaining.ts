import {$t} from "@/plugins/i18n";

const Layout = () => import("@/layout/index.vue");

export default [
    {
        path: "/auth/authLoginTest",
        name: "authLoginTest",
        component: () => import("@/views/authLogin/test.vue"),
        meta: {
            title: '授权登录测试',
            showLink: false,
            rank: 100
        }
    },
    {
        path: "/auth/authLogin",
        name: "authLogin",
        component: () => import("@/views/authLogin/index.vue"),
        meta: {
            title: '授权登录',
            showLink: false,
            rank: 100
        }
    },
    {
        path: "/result",
        name: "Result",
        component: () => import("@/components/result/result.vue"),
        meta: {
            title: '结果页',
            showLink: false,
            rank: 100
        }
    },
    {
        path: "/resultWithAutoClose",
        name: "ResultWithAutoClose",
        component: () => import("@/components/result/resultWithAutoClose.vue"),
        meta: {
            title: '结果页',
            showLink: false,
            rank: 100
        }
    },
    {
        path: "/login",
        name: "Login",
        component: () => import("@/views/login/index.vue"),
        meta: {
            title: $t("menus.hslogin"),
            showLink: false,
            rank: 101
        }
    },
    {
        path: "/redirect",
        component: Layout,
        meta: {
            title: $t("status.hsLoad"),
            showLink: false,
            rank: 102
        },
        children: [
            {
                path: "/redirect/:path(.*)",
                name: "Redirect",
                component: () => import("@/layout/redirect.vue")
            }
        ]
    }
] satisfies Array<RouteConfigsTable>;
