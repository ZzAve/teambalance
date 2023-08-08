const path = require("path");
const HtmlWebPackPlugin = require("html-webpack-plugin"); // eslint-disable-line import/no-extraneous-dependencies
const CopyWebpackPlugin = require("copy-webpack-plugin");
const webpack = require("webpack");

const htmlPlugin = new HtmlWebPackPlugin({
  template: path.join(__dirname, "src/main/react/index.html"),
  filename: "./index.html"
});

const copyWebpackPlugin = new CopyWebpackPlugin({
  patterns: [
    // "src/main/react/images"
    { from: "react/images", to: "images", context: "src/main" },
    { from: "react/manifest.json", to: "./", context: "src/main" }
    // {from: "src/main/react/manifest.json", to: "./" }
  ]
});

module.exports = {
  mode: "production",
  entry: [
    path.join(__dirname, "src/main/react/index.tsx"),
    path.join(__dirname, "src/main/react/polyfill.ts")
  ],
  output: {
    path: path.join(process.cwd(), "target/classes/static"),
    filename: "[name].bundle.js",
    chunkFilename: "[name].bundle.js",
    publicPath: "/"
  },
  optimization: {
    runtimeChunk: true,
    splitChunks: {
      chunks: "all"
    }
  },
  cache: {
    type: "filesystem",
    maxAge: 5184000000, // one month
    buildDependencies: {
      // This makes all dependencies of this file - build dependencies
      config: [__filename]
      // By default webpack and loaders are build dependencies
    }
  },
  resolve: {
    extensions: [".jsx", ".ts", ".js", ".tsx"],
    symlinks: false,
    fallback: {
      stream: require.resolve("stream-browserify"),
      buffer: require.resolve("buffer")
    }
  },
  devtool: "eval-cheap-module-source-map",
  module: {
    rules: [
      {
        test: /\.ts|tsx$/,
        include: path.resolve(__dirname, "src"),
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            plugins: ["@babel/plugin-proposal-class-properties"],
            presets: ["@babel/preset-env", "@babel/preset-react"]
          }
        }
      },
      {
        test: /\.tsx?$/,
        use: [
          {
            loader: "ts-loader",
            options: {
              transpileOnly: true
            }
          }
        ],
        include: path.resolve(__dirname, "src"),
        exclude: /node_modules/
      },
      {
        test: /\.css$/i,
        include: path.resolve(__dirname, "src"),
        use: ["style-loader", "css-loader"]
      }
    ]
  },

  plugins: [
    htmlPlugin,
    copyWebpackPlugin,
    // Work around for Buffer is undefined:
    // https://github.com/webpack/changelog-v5/issues/10
    new webpack.ProvidePlugin({
      Buffer: ["buffer", "Buffer"]
    }),
    new webpack.ProvidePlugin({
      process: "process/browser"
    })
  ],

  devServer: {
    historyApiFallback: true,
    allowedHosts: [
      ".teambalance.local"
    ],
    port: 3000,
    proxy: [
      {
        context: ["/api/**"],
        target: "http://localhost:8080"
      }
    ]
  }
};
