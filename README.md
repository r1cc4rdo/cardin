# Cardin gateway public API
What follows is a [minimal implementation](https://github.com/r1cc4rdo/cardin/blob/main/cardin.py) of the "public" Cardin gateway API with no dependencies.
``` python
import time
import requests

class Cardin:    
    base_url = 'https://gateway.cardin.it/publicapi/v3'
    headers = {
        'User-Agent': ('Mozilla/5.0 (Linux; Android 13; Pixel 6a) AppleWebKit/537.36 '
                       '(KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36'),
        'Content-type': 'application/json; charset=UTF-8',
        'Accept': 'application/json; charset=UTF-8',
        'Cache-Control': 'no-cache'}
    
    def __init__(self, user_credentials):
        self.credentials = user_credentials  # ('username', 'password')

    def __call__(self, endpoint, **params):
        url = f'{Cardin.base_url}/{endpoint}?_={int(time.time() * 1000)}'
        response = requests.post(url, json=params, headers=Cardin.headers, auth=self.credentials, timeout=120)
        response.raise_for_status()
        return response.json()

    def receiver(self, device_credentials):  # ('device_id', 'device_pin')
        credentials_dict = dict(zip(('serialKey', 'pin'), device_credentials))
        
        def receiver_fun(endpoint, **params):
            return self(f'receiver/{endpoint}', context='cardin', **credentials_dict, **params)
        
        return receiver_fun
```
which you can use as follows:
``` python
cardin = Cardin(('your username', 'your password'))
print(cardin('account/checkgdpr'))
```
``` python
receiver = cardin.receiver(('your device id', 'your device pin'))
print(receiver('status', activeHigh='true', deviceRead='false'))
print(receiver('activatechannel', channel='A', deviceType='R'))
```
In order to use this code, you will need both user and device credentials.

## API endpoints
To my knowledge there is no official documentation for the API. If anyone at Cardin is listening, an open officially supported API standard would surely attract and retain more customers :)

The [CRD ONE](https://play.google.com/store/apps/details?id=it.cardin.cardinremotecontrol) app is written using the [Apache Cordova](https://cordova.apache.org/) framework.
It should be technically possible to run the Cordova app extracted from the .apk on desktop with no modifications, but I had no luck trying.

Within the source code, under ```assets/www```:
* ```webservice/wshandler.js``` defines the API endpoints,
* ```webservice/requestws.js``` contains the ```execute``` function to call them, and
* ```config.js``` and ```constants.js``` have useful definitions and defaults.

I cannot test any other endpoints since than those covered above since I don't have the corresponding device. If you successfully verify others, send a [pull request](https://github.com/r1cc4rdo/cardin/pulls)!

## Links
* [Cardin website](https://www.cardin.it)
* [Cardin software downloads](https://www.cardin.it/it/assistenza/software-download)
* [CRD ONE app](https://play.google.com/store/apps/details?id=it.cardin.cardinremotecontrol) on Google's Play Store
* [CRD ONE guide](https://gateway.cardin.it/public/files/crdone-guide.pdf) (pdf)
* [Apache Cordova](https://cordova.apache.org/)
