module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-typescript');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-sass');

    grunt.initConfig({

        typescript: {
            api: {
                src: ['src/main/webapp/admin2/api/js/main.ts'],
                dest: 'src/main/webapp/admin2/api/js/api.js',
                options: {
                    // target: 'es5',
                    sourcemap: true,
                    declaration: true
                }
            },
            api_test: {
                src: ['src/test/webapp/admin2/api/js/test.ts'],
                dest: 'src/test/webapp/admin2/api/js/test.js',
                options: {
                    // target: 'es5',
                    sourcemap: true,
                    declaration: true
                }
            },
            space_manager: {
                src: ['src/main/webapp/admin2/apps/space-manager/js/main.ts'],
                dest: 'src/main/webapp/admin2/apps/space-manager/js/all.js',
                options: {
                    // target: 'es5',
                    sourcemap: true
                }
            },
            content_manager: {
                src: ['src/main/webapp/admin2/apps/content-manager/js/main.ts'],
                dest: 'src/main/webapp/admin2/apps/content-manager/js/all.js',
                options: {
                    // target: 'es5',
                    sourcemap: true
                }
            },
            live_edit: {
                src: ['src/main/webapp/admin2/live-edit/js/Main.ts'],
                dest: 'src/main/webapp/admin2/live-edit/js/all.js',
                options: {
                    // target: 'es5',
                    sourcemap: true
                }
            }
        },

        watch: {
            files: ['src/main/webapp/admin2/**/*.ts', 'src/test/webapp/admin2/**/*.js'],
            tasks: ['typescript']
        }

    });

    grunt.registerTask('default', 'watch');

};
