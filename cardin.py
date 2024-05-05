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
    
    def __init__(self, user_credentials):  # ('username', 'password')
        self.credentials = user_credentials

    def __call__(self, endpoint, **params):
        url = f'{Cardin.base_url}/{endpoint}?_={int(time.time() * 1000)}'
        response = requests.post(url, json=params, headers=Cardin.headers, auth=self.credentials, timeout=120)
        response.raise_for_status()
        return response.json()

    def receiver(self, device_credentials):  # ('device_id', 'device_pin')
        fixed_params = dict(zip(('serialKey', 'pin'), device_credentials)) | {'context': 'cardin'}
        return lambda endpoint, **params: self(f'receiver/{endpoint}', **fixed_params, **params)


if __name__ == '__main__':
    from pprint import pprint

    cardin = Cardin(('your username', 'your password'))
    pprint(cardin('account/checkgdpr'))

    receiver = cardin.receiver(('your device id', 'your device pin'))
    pprint(receiver('status', activeHigh='true', deviceRead='false'))
    pprint(receiver('activatechannel', channel='A', deviceType='R'))
