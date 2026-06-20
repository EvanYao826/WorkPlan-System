import request from './request'

export function getLeaders() {
  return request.get('/user/leaders')
}
