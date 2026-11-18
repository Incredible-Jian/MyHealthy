import request from '@/utils/request'
export const call = (url, method='get', data, params) => request({url:'/health/'+url,method,data,params})
