const path = require("path");
const HtmlWebPackPlugin = require("html-webpack-plugin"); // eslint-disable-line import/no-extraneous-dependencies
const CopyWebpackPlugin = require("copy-webpack-plugin");
const webpack = require("webpack");

const htmlPlugin = new HtmlWebPackPlugin({
  template: path.join(__dirname, "src/main/react/index.html"),
  filename: "./index.html",
});

const copyWebpackPlugin = new CopyWebpackPlugin({
  patterns: [
    // "src/main/react/images"
    { from: "react/images", to: "images", context: "src/main" },
    { from: "react/manifest.json", to: "./", context: "src/main" },
    // {from: "src/main/react/manifest.json", to: "./" }
  ],
});

module.exports = {
  mode: "production",
  entry: [
    path.join(__dirname, "src/main/react/index.tsx"),
    path.join(__dirname, "src/main/react/polyfill.ts"),
  ],
  output: {
    path: path.join(process.cwd(), "target/classes/static"),
    filename: "[name].bundle.js",
    chunkFilename: "[name].bundle.js",
    publicPath: "/",
  },
  optimization: {
    splitChunks: {
      chunks: "all",
    },
  },
  resolve: {
    extensions: [".jsx", ".ts", ".js", ".tsx"],
    fallback: {
      stream: require.resolve("stream-browserify"),
      buffer: require.resolve("buffer"),
    },
  },
  devtool: "cheap-module-source-map",
  module: {
    rules: [
      {
        test: /\.js|jsx$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            plugins: ["@babel/plugin-proposal-class-properties"],
            presets: ["@babel/preset-env", "@babel/preset-react"],
          },
        },
      },
      {
        test: /\.tsx?$/,
        use: "ts-loader",
        exclude: /node_modules/,
      },
      {
        test: /\.css$/i,
        use: ["style-loader", "css-loader"],
      },
    ],
  },

  plugins: [
    htmlPlugin,
    copyWebpackPlugin,
    // Work around for Buffer is undefined:
    // https://github.com/webpack/changelog-v5/issues/10
    new webpack.ProvidePlugin({
      Buffer: ["buffer", "Buffer"],
    }),
    new webpack.ProvidePlugin({
      process: "process/browser",
    }),
  ],

  devServer: {
    historyApiFallback: true,
    port: 3000,
    proxy: {
      "/api/**": "http://localhost:8080",
      "/oauth2/**": "http://localhost:8080",
      "/login/**": "http://localhost:8080",
      "/login": "http://localhost:8080",
      "/logout": "http://localhost:8080",
    },
  },
};
