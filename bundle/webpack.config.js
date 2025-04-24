const path = require('path');

module.exports = {
  entry: './src/index.mjs',
  resolve: {
   useSyncFileSystemCalls: true,
    fallback: {
      url: false,
      path: false,
    },
  },
  output: {
    filename: 'main.js',
    path: path.resolve(__dirname, 'dist'),
     publicPath: '',
  },

};