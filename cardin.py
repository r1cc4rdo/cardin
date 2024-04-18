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

    def receiver(self, device_credentials):  # ('device_id', 'device_pin')
        credentials_dict = dict(zip(('serialKey', 'pin'), device_credentials))
        
        def receiver_fun(endpoint, **params):
            return self(f'receiver/{endpoint}', context='cardin', **credentials_dict, **params)
        
        return receiver_fun


if __name__ == '__main__':
    from pprint import pprint

    cardin = Cardin(('your username', 'your password'))
    pprint(cardin('account/checkgdpr'))

    receiver = cardin.device(('your device id', 'your device pin'))
    pprint(receiver('status', activeHigh='true', deviceRead='false'))
    pprint(receiver('activatechannel', channel='A', deviceType='R'))