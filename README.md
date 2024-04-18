# Cardin gateway public API
What follows is a minimal implementation of the "public" Cardin gateway API with no dependencies.
``` python
import time
import requests

class Cardin:    
    base_url = 'https://gateway.cardin.it/publicapi/v3'

    headers = {
        'User-Agent': 'cardin',  # arbitrarily chosen, to substitute Requests' default
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

    def device(self, device_credentials):  # ('device_id', 'device_pin')
        credentials_dict = dict(zip(('serialKey', 'pin'), device_credentials))
        
        def device_fun(endpoint, **params):
            return self(endpoint, context='cardin', **credentials_dict, **params)
        
        return device_fun
```
which you can use as follows:
``` python
cardin = Cardin(('your username here', 'your password here'))
print(cardin('account/checkgdpr'))
```
``` python
receiver = cardin.device(('your device id', 'your device pin'))
print(receiver('receiver/status', activeHigh='true', deviceRead='false'))
print(receiver('receiver/activatechannel', channel='A', deviceType='R'))
```
You will need to bring your own user and device credentials to use.

## API endpoints
To my knowledge there is no official documentation for the API. If anyone at Cardin is listening, an open officially supported API standard would surely attract and retain more customers :)

The [CRD ONE](https://play.google.com/store/apps/details?id=it.cardin.cardinremotecontrol) app is written using the [Apache Cordova](https://cordova.apache.org/) framework.
It should be technically possible to run the Cordova app extracted from the .apk on desktop with no modifications, but I had no luck trying.

Within the source code, under ```assets/www```:
* ```webservice/wshandler.js``` defines the API endpoints,
* ```webservice/requestws.js``` contains the ```execute``` function to call them, and
* ```config.js``` and ```constants.js``` have useful definitions and defaults.

## Links
* [Cardin website](https://www.cardin.it)
* [Cardin software downloads](https://www.cardin.it/it/assistenza/software-download)
* [CRD ONE app](https://play.google.com/store/apps/details?id=it.cardin.cardinremotecontrol) on Google's Play Store
* [CRD ONE guide](https://gateway.cardin.it/public/files/crdone-guide.pdf) (pdf)
* [Apache Cordova](https://cordova.apache.org/)
