const path = require('path');
const R = require('ramda');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const webpack = require('webpack')
const TerserPlugin = require('terser-webpack-plugin');
const {
    setEntry,
    setEntryForPath,
    addRule,
    addPlugin,
    appendExtensions,
    prependExtensions
} = require('./util/compose');
const env = require('./util/env');

const isProd = env.prod;

// ----------------------------------------------------------------------------
// Base config
// ----------------------------------------------------------------------------

const config = {
    context: path.join(__dirname, '/src/main/resources/assets'),
    entry: {},
    output: {
        path: path.join(__dirname, '/build/resources/main/assets'),
        filename: '[name].js',
    },
    resolve: {
        extensions: [],
    },
    optimization: {
        minimizer: [
            new TerserPlugin({
                terserOptions: {
                    compress: {
                        drop_console: false,
                    },
                },
            }),
        ],
        splitChunks: {
            minSize: 30000,
        },
    },
    plugins: [],
    mode: env.type,
    devtool: isProd ? false : 'source-map',
};

// ----------------------------------------------------------------------------
// JavaScript loaders
// ----------------------------------------------------------------------------

// BABEL
function addBabelSupport(cfg) {
    const rule = {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: 'babel-loader',
        options: {
            babelrc: false,
            plugins: [],
            presets: [
                [
                    '@babel/preset-env',
                    {
                        // false means polyfill not required runtime
                        useBuiltIns: false
                    },
                ],
            ]
        }
    };

    return R.pipe(
        setEntry('../js/main', './src/js/main.js'),
        addRule(rule),
        prependExtensions(['es6', '.jsx', '.js', '.json'])
    )(cfg);
}

// ----------------------------------------------------------------------------
// CSS loaders
// ----------------------------------------------------------------------------

const createDefaultCssLoaders = () =>
    ([
        {loader: MiniCssExtractPlugin.loader, options: {publicPath: '../'}},
        {loader: 'css-loader', options: {sourceMap: !isProd, importLoaders: 1}},
        {loader: 'postcss-loader', options: {sourceMap: !isProd}},
    ]);

const createCssPlugin = () =>
    (
        new MiniCssExtractPlugin({
            filename: 'css/main.css',
            chunkFilename: '[id].css',
        })
    );

// SASS & SCSS
function addSassSupport(cfg) {
    const rule = {
            test: /\.(sass|scss)$/,
            use: [
                ...createDefaultCssLoaders(),
                {
                    loader: 'sass-loader', options:
                        {
                            sourceMap: !isProd
                        }
                }
            ]
        };

    const plugin = createCssPlugin();

    return R.pipe(
        addRule(rule),
        addPlugin(plugin),
        appendExtensions(['.sass', '.scss', '.css'])
    )(cfg);
}

function addGlobalJQuery(cfg) {
    return R.pipe(
        addPlugin(
            new webpack.ProvidePlugin({
                $: "jquery",
                jQuery: "jquery"
            })
        )
    )(cfg);
}

// ----------------------------------------------------------------------------
// Resource loaders
// ----------------------------------------------------------------------------

// FONTS IN CSS
function addFontSupport(cfg) {
    const rule = {
        test: /\.(eot|woff|woff2|ttf|svg)$/,
        use: 'file-loader?name=fonts/[name].[ext]'
    };

    return R.pipe(
        addRule(rule)
    )(cfg);
}

// ----------------------------------------------------------------------------
// Result config
// ----------------------------------------------------------------------------

module.exports = R.pipe(
    addGlobalJQuery,
    addBabelSupport,
    addSassSupport,
    addFontSupport
)(config);
