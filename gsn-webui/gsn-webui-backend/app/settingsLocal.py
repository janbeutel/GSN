# See https://docs.djangoproject.com/en/1.8/ref/settings/#databases

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': 'db.sqlite3', }
}

GSN = {
    'CLIENT_ID': 'gsn-webui-backend',
    'CLIENT_SECRET': 'gsn-webui-backend',
    'SERVICE_URL_PUBLIC': 'http://walker.uibk.ac.at:9000/ws/', # used for in-browser redirects
    'SERVICE_URL_LOCAL': 'http://walker.uibk.ac.at:9000/ws/',  # used for on-server direct calls
    'WEBUI_URL': 'http://walker.uibk.ac.at:4200/',             # used for in-browser redirects
    'MAX_QUERY_SIZE': 5000,
}