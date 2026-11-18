from pathlib import Path
import urllib.request,urllib.error,json,time,subprocess,socket,base64,os
R=Path(__file__).resolve().parents[1];results=[]
def req(path,method='GET',body=None,token=None):
 h={'Content-Type':'application/json'}
 if token:h['Authorization']='Bearer '+token
 r=urllib.request.Request('http://127.0.0.1:18080'+path,data=None if body is None else json.dumps(body).encode(),headers=h,method=method)
 try:
  with urllib.request.urlopen(r,timeout=30)as f:return json.load(f)
 except urllib.error.HTTPError as e:return json.load(e)
def login(u):return req('/login','POST',{'username':u,'password':'admin123','code':'','uuid':''})['token']
def sql(q):
 r=subprocess.run(['docker','exec','-i','myhealthy-course-mysql-1','sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" mysql -N -B -umyhealthy myhealthy_course'],input="SET NAMES utf8mb4;\n"+q,text=True,encoding='utf-8',capture_output=True,check=True);return r.stdout.strip()
def check(name,c,detail=''):
 results.append({'name':name,'passed':bool(c),'detail':detail});print(('PASS 'if c else'FAIL ')+name,flush=True);(R/'docs/supplemental-results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2),encoding='utf-8')
def connect(ticket):
 s=socket.create_connection(('127.0.0.1',18080),timeout=8);key=base64.b64encode(os.urandom(16)).decode();s.sendall((f'GET /health/chat/socket?ticket={ticket} HTTP/1.1\r\nHost: 127.0.0.1:18080\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Version: 13\r\nSec-WebSocket-Key: {key}\r\nOrigin: http://127.0.0.1:18082\r\n\r\n').encode());response=b''
 while b'\r\n\r\n' not in response:response+=s.recv(4096)
 return s,response
p=login('zhangsan');doc=login('healthteacher');admin=login('admin')
t=req('/health/chat/ticket',token=p)['data'];ws,response=connect(t)
check('WebSocket一次性票据握手成功',b'101' in response.split(b'\r\n')[0]);ws2,reuse=connect(t);check('WebSocket票据不可重复使用',b'101'not in reuse.split(b'\r\n')[0]);ws2.close();ws.close()
ds,dh=connect(req('/health/chat/ticket',token=doc)['data'])
check('健康师在线握手成功',b'101' in dh.split(b'\r\n')[0])
req('/health/chat/request','POST',{},p);notice=ds.recv(4096)
check('WebSocket真实推送队列刷新',b'refresh' in notice)
q=req('/health/chat/queue',token=p)['data'][0];check('患者可看到健康师在线',q['practitionerOnline'] is True)
req('/health/chat/'+str(q['id'])+'/close','POST',{},p);ds.close()
check('健康师不可读取未绑定用户记录',req('/health/measurement/list?userId=10000',token=doc).get('code')!=200)
check('AI未配置状态准确',req('/health/ai/status',token=p)['data']['configured'] is False)
help=req('/health/ai/consult','POST',{'question':'如何查看记录？'},p)
check('本地帮助不冒充模型回答',help['data']['source']=='local_help' and '固定' in help['data']['answer'])
check('未配置时拒绝生成伪AI报告',req('/health/ai/report','POST',{},p).get('code')!=200)
for mod in ['secgoods','secorder','secpay']:
 check(mod+'管理员模块可查询',req('/health/'+mod+'/list',token=admin).get('code')==200)
 check(mod+'患者不可读取全量管理数据',req('/health/'+mod+'/list',token=p).get('code')!=200)
gid=int(time.time());sql(f"INSERT INTO t_seckill_goods(id,title,price,stock,start_time,end_time) VALUES({gid},'超时回补验收',1,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_ADD(NOW(),INTERVAL 1 DAY));")
oid=req('/health/shop/reserve/'+str(gid),'POST',{},p)['data'];sql(f"UPDATE t_seckill_order SET expires_at=DATE_SUB(NOW(),INTERVAL 1 MINUTE) WHERE id='{oid}';")
for _ in range(25):
 state=sql(f"SELECT status FROM t_seckill_order WHERE id='{oid}';")
 if state=='EXPIRED':break
 time.sleep(1)
check('30分钟超时扫描机制回补库存',state=='EXPIRED' and sql(f'SELECT stock FROM t_seckill_goods WHERE id={gid};')=='1','通过专用测试订单将到期时间置于过去，观察定时扫描；未实际等待30分钟')
check('过期订单不可结算',req('/health/shop/orders/'+oid+'/demo-pay','POST',{},p).get('code')!=200)
req('/health/shop/orders/'+oid+'/cancel','POST',{},p)
check('过期后重复取消不重复回补',sql(f'SELECT stock FROM t_seckill_goods WHERE id={gid};')=='1')
for name in ['recording','trends']:
 media=R/f'newui/public/media/{name}.mp4'
 info=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_entries','stream=width,height:format=duration','-of','json',str(media)],text=True,encoding='utf-8'))
 check(name+'原创视频规格',any(x.get('width')==1280 and x.get('height')==720 for x in info['streams']) and 11<float(info['format']['duration'])<13,info['format']['duration']+' 秒，1280x720')
 with urllib.request.urlopen('http://127.0.0.1:18082/media/'+name+'.mp4')as f:check(name+'视频资源可加载',f.status==200)
print(json.dumps({'total':len(results),'passed':sum(x['passed']for x in results)}))
raise SystemExit(0 if all(x['passed'] for x in results)else 1)
