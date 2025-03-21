const react = require("eslint-plugin-react")
const reactHooks = require("eslint-plugin-react-hooks")
const nx = require("@nx/eslint-plugin")
const imports = require("eslint-plugin-import")
const jsxA11y = require("eslint-plugin-jsx-a11y")
const perfectionist = require("eslint-plugin-perfectionist")
const unusedImports = require("eslint-plugin-unused-imports")
const formatjs = require("eslint-plugin-formatjs")
const globals = require("globals")

module.exports = [
    ...nx.configs["flat/base"],
    ...nx.configs["flat/typescript"],
    ...nx.configs["flat/javascript"],
    jsxA11y.flatConfigs.recommended,
    {
        ignores: ["**/dist"],
    },
    {
        files: ["**/*.ts", "**/*.tsx", "**/*.js", "**/*.jsx"],
        plugins: {
            formatjs,
            "unused-imports": unusedImports,
            import: imports,
            perfectionist,
            react,
            "react-hooks": reactHooks,
        },
        languageOptions: {
            parserOptions: {
                ecmaFeatures: {
                    jsx: true,
                },
            },
            globals: {
                ...globals.browser,
            },
        },
        rules: {
            "react/jsx-boolean-value": "error",
            "react/jsx-curly-brace-presence": "warn",
            "react/jsx-fragments": "warn",
            "react/jsx-sort-props": [
                "warn",
                {
                    callbacksLast: true,
                    shorthandFirst: true,
                    shorthandLast: false,
                    ignoreCase: true,
                    noSortAlphabetically: false,
                    reservedFirst: true,
                },
            ],
            "react/self-closing-comp": "error",

            "react-hooks/exhaustive-deps": "error",
            "react-hooks/rules-of-hooks": "error",
            "@typescript-eslint/no-unused-vars": "off",
            "import/first": "error",
            "import/newline-after-import": "error",
            "import/no-anonymous-default-export": "error",
            "import/no-duplicates": "error",
            "import/no-named-default": "error",
            "import/no-self-import": "error",
            "import/no-useless-path-segments": [
                "error",
                {
                    noUselessIndex: true,
                },
            ],
            "perfectionist/sort-imports": [
                "error",
                {
                    type: "natural",
                    order: "asc",
                    groups: [
                        "client-server-only",
                        "react",
                        ["builtin", "external"],
                        ["internal-type", "internal"],
                        ["parent", "sibling", "index"],
                        ["type", "parent-type", "sibling-type", "index-type"],
                        "side-effect",
                        "style",
                        "unknown",
                    ],
                    customGroups: {
                        value: {
                            react: ["react", "react-*"],
                            "client-server-only": ["client-only", "server-only"],
                        },
                        type: {
                            react: "react",
                        },
                    },
                    newlinesBetween: "never",
                },
            ],
            "perfectionist/sort-named-imports": [
                "error",
                {
                    type: "natural",
                    order: "asc",
                },
            ],
            "unused-imports/no-unused-imports": "warn",
            "unused-imports/no-unused-vars": [
                "warn",
                {
                    vars: "all",
                    varsIgnorePattern: "^_",
                    args: "after-used",
                    argsIgnorePattern: "^_",
                },
            ],
            "max-params": ["error", { max: 4 }],
            "no-console": ["warn", { allow: ["warn", "error", "assert"] }],
            "no-eval": "error",
            "no-useless-rename": "error",

            "@typescript-eslint/no-empty-function": "off",
            "@typescript-eslint/no-empty-object-type": "off",
            "@typescript-eslint/no-explicit-any": "off",

            "perfectionist/sort-array-includes": [
                "error",
                {
                    type: "natural",
                    order: "asc",
                },
            ],
            "perfectionist/sort-intersection-types": [
                "error",
                {
                    type: "natural",
                    order: "asc",
                },
            ],
            "perfectionist/sort-named-exports": [
                "error",
                {
                    type: "natural",
                    order: "asc",
                },
            ],
            "perfectionist/sort-union-types": [
                "error",
                {
                    type: "natural",
                    order: "asc",
                    groups: ["unknown", "nullish"],
                },
            ],
            "@nx/enforce-module-boundaries": [
                "error",
                {
                    enforceBuildableLibDependency: true,
                    allow: ["^.*/eslint(\\.base)?\\.config\\.[cm]?js$"],
                    depConstraints: [
                        {
                            sourceTag: "*",
                            onlyDependOnLibsWithTags: ["*"],
                        },
                    ],
                },
            ],
            "formatjs/enforce-id": ["error", { idInterpolationPattern: "[sha512:contenthash:base64:6]" }],
        },
    },
]
