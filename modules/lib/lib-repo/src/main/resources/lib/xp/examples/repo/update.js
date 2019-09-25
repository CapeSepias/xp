/* global require, Java, testInstance*/
const repoLib = require('/lib/xp/repo.js');
const valueLib = require('/lib/xp/value');
const assert = require('/lib/xp/testing.js');

const stream = testInstance.createByteSource('Hello World');

// Editor to call for repo.
function editor(repoData) {

    repoData.myString = 'modified';
    repoData.myArray = ['modified1', 'modified2', 'modified3'];

    repoData.myBinaryReference = valueLib.binary('myFile', stream);

    delete repoData.toBeRemoved;

    return repoData;
}

// BEGIN
// Update data
const result = repoLib.updateRepository({
    editor: editor,
    id: 'my-repo'
});
// END