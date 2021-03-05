module.exports = {
    purge: {
        //enabled: true,
        content: ['./resources/**/*.html',
            './resources/**/*.js'
        ],
    },
    theme: {
        extend: {},
    },
    variants: {
        extend: {
            
           borderWidth: ['hover', 'focus']
          }
    },
    plugins: [],
}