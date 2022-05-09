import axios from 'axios'

const LOSSLESS_REST_API_URL = 'http://localhost:8080/api/compress/lossless';

class Lossless {

    getUsers(){
        return axios.get(LOSSLESS_REST_API_URL);
    }
}

export default new Lossless();