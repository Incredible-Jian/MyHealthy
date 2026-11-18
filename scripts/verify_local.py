from pathlib import Path
import urllib.request, urllib.error, json, concurrent.futures, time, subprocess, os
R=Path(__file__).resolve().parents[1]
BASE='http://127.0.0.1:18080'
results=[]
def request(path,method='GET',data=None,token=None):
    h={'Content-Type':'application/json'}
    if token:h['Authorization']='Bearer '+token
    req=urllib.request.Request(BASE+path,data=None if data is None else json.dumps(data).encode(),headers=h,method=method)
    try:
        with urllib.request.urlopen(req,timeout=30) as f:return json.load(f)
    except urllib.error.HTTPError as e:
        try:return json.load(e)
        except:return {'code':e.code}
def login(user):
    x=request('/login','POST',{'username':user,'password':'admin123','code':'','uuid':''})
    if 'token' not in x:raise AssertionError('Login failed for '+user+': '+str(x))
    return x['token']
def check(name,condition,detail=''):
    results.append({'name':name,'passed':bool(condition),'detail':detail});print(('PASS ' if condition else 'FAIL ')+name,flush=True)
    (R/'docs/test-results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2),encoding='utf-8')
def sql(query):
    proc=subprocess.run(['docker','exec','-i','myhealthy-course-mysql-1','sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" mysql -N -B -umyhealthy myhealthy_course'],input="SET NAMES utf8mb4;\n"+query,text=True,encoding='utf-8',capture_output=True)
    if proc.returncode:raise RuntimeError(proc.stderr)
    return proc.stdout.strip()
for _ in range(45):
    try:
        if request('/captchaImage').get('code')==200:break
    except Exception:time.sleep(1)
tokens={u:login(u) for u in ['admin','zhangsan','lisi','healthteacher','wangwu']}
p=tokens['zhangsan'];other=tokens['lisi'];doc=tokens['healthteacher']
check('五个演示账号登录',len(tokens)==5)
check('匿名请求被拒绝',request('/health/workflow/dashboard').get('code')!=200)
for kind in ['pressure','glucose','lipid','ecg']:
    r=request('/health/workflow/simulate/'+kind,'POST',{'age':40,'heightCm':170,'weightKg':65,'durationSeconds':60,'mealState':'fasting'},p)
    check(kind+'模拟写入',r.get('code')==200 and r.get('data',{}).get('source')=='local_simulation')
dash=request('/health/workflow/dashboard',token=p).get('data',{})
check('首页读取四类最新指标',all(dash.get(k) for k in ['pressure','glucose','lipid','ecg']))
mid=dash['pressure']['id']
check('跨患者记录访问被拒绝',request('/health/measurement/'+str(mid),token=other).get('code')!=200)
check('伪造userId查询被拒绝',request('/health/measurement/list?userId=1000',token=other).get('code')!=200)
check('采集记录手工修改被拒绝',request('/health/measurement','PUT',{'id':mid,'systolic':999},p).get('code')!=200)
check('无效模拟参数被拒绝',request('/health/workflow/simulate/pressure','POST',{'age':-1,'heightCm':0,'weightKg':65},p).get('code')!=200)
check('10天趋势读取',len(request('/health/workflow/trends?kind=pressure',token=p).get('data',[]))>0)
check('运动步数一致性校验',request('/health/workflow/sports','POST',{'steps':100,'durationSeconds':1},p).get('code')!=200)
check('运动记录保存',request('/health/workflow/sports','POST',{'steps':10,'durationSeconds':10},p).get('code')==200)
check('生成本地规则报告',request('/health/workflow/report','POST',{},p).get('code')==200)
reports=request('/health/workflow/reports',token=p)['data'];rid=reports[0]['id']
check('报告明确标记来源',reports[0]['source']=='local_rules')
check('患者不能审核报告',request(f'/health/workflow/report/{rid}/review','POST',{'content':'untrusted'},p).get('code')!=200)
check('绑定健康师可以审核',request(f'/health/workflow/report/{rid}/review','POST',{'content':'教学审核记录：请核对模拟数据来源。'},doc).get('code')==200)
check('审核状态持久化',request('/health/workflow/reports',token=p)['data'][0]['status']=='approved')
check('健康师访问绑定患者记录',request('/health/measurement/list?userId=1000',token=doc).get('code')==200)
check('健康师建立随访',request('/health/record','POST',{'userId':1000,'practitionerId':1100,'content':'教学随访测试'},doc).get('code')==200)
check('患者本人建立用药记录',request('/health/medichine','POST',{'medicineName':'教学占位药名','dosage':'仅作记录测试','frequency':'教学','notes':'非处方'},p).get('code')==200)
profile=request('/health/tuser/list',token=p)['rows'][0]
profile['practitionerId']=1200
request('/health/tuser','PUT',profile,p)
check('患者不能自行改绑健康师',request('/health/tuser/list',token=p)['rows'][0]['practitionerId']==1100)
stamp=int(time.time())
sql(f"INSERT INTO t_seckill_goods(id,title,price,stock,start_time,end_time) VALUES({stamp},'并发验收专用商品',1,20,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_ADD(NOW(),INTERVAL 1 DAY));")
seed=[]
for i in range(120):
    uid=10000+i
    seed.append(f"INSERT IGNORE INTO sys_user(user_id,dept_id,user_name,nick_name,user_type,password,status,del_flag,create_time) SELECT {uid},103,'course_load_{i}','课程测试{i}','00',password,'0','0',NOW() FROM sys_user WHERE user_id=1;")
    seed.append(f"INSERT IGNORE INTO sys_user_role(user_id,role_id) VALUES({uid},100);")
sql('\n'.join(seed))
with concurrent.futures.ThreadPoolExecutor(max_workers=20) as pool:loadtokens=list(pool.map(login,['course_load_'+str(i) for i in range(120)]))
start=time.monotonic()
with concurrent.futures.ThreadPoolExecutor(max_workers=40) as pool:
    rs=list(pool.map(lambda tok:request('/health/shop/reserve/'+str(stamp),'POST',{},tok),loadtokens))
success=sum(r.get('code')==200 for r in rs)
counts=sql(f"SELECT stock,(SELECT COUNT(*) FROM t_seckill_order WHERE goods_id={stamp}) FROM t_seckill_goods WHERE id={stamp};")
check('120请求40并发库存20不超卖',success==20 and counts=='0\t20',f'success={success}; stock/orders={counts}; elapsed={time.monotonic()-start:.2f}s')
# One user duplicate requests must result in a single durable reservation.
gid=stamp+1
sql(f"INSERT INTO t_seckill_goods(id,title,price,stock,start_time,end_time) VALUES({gid},'重复与退单验收',2,20,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_ADD(NOW(),INTERVAL 1 DAY));")
with concurrent.futures.ThreadPoolExecutor(max_workers=10) as pool:rs=list(pool.map(lambda _:request('/health/shop/reserve/'+str(gid),'POST',{},p),range(10)))
check('同用户10次并发仅一笔预留',sum(x.get('code')==200 for x in rs)==1)
oid=next(x['data'] for x in rs if x.get('code')==200)
check('其他患者不能取消订单',request('/health/shop/orders/'+oid+'/cancel','POST',{},other).get('code')!=200)
for i in range(3):
    if i:oid=request('/health/shop/reserve/'+str(gid),'POST',{},p)['data']
    request('/health/shop/orders/'+oid+'/cancel','POST',{},p)
    request('/health/shop/orders/'+oid+'/cancel','POST',{},p)
check('重复取消不重复回补库存',sql(f'SELECT stock FROM t_seckill_goods WHERE id={gid};')=='20')
check('同商品退单三次后被限制',request('/health/shop/reserve/'+str(gid),'POST',{},p).get('code')!=200)
gid2=stamp+2
sql(f"INSERT INTO t_seckill_goods(id,title,price,stock,start_time,end_time) VALUES({gid2},'结算验收',3,5,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_ADD(NOW(),INTERVAL 1 DAY));")
r=request('/health/shop/reserve/'+str(gid2),'POST',{},p)
check('退单次数不影响其他商品',r.get('code')==200)
oid=r['data']
check('未配置支付宝明确报错',request('/health/shop/orders/'+oid+'/checkout','POST',{},p).get('code')!=200)
request('/health/shop/orders/'+oid+'/demo-pay','POST',{},p);request('/health/shop/orders/'+oid+'/demo-pay','POST',{},p)
check('本地演示结算幂等记账',sql(f"SELECT COUNT(*),MIN(provider) FROM t_seckill_payment WHERE order_id='{oid}';")=='1\tLOCAL_DEMO')
check('已付款订单不允许退单',request('/health/shop/orders/'+oid+'/cancel','POST',{},p).get('code')!=200)
# Real consultation endpoints and ownership checks.
for tok in [p,other]:
    for c in request('/health/chat/queue',token=tok).get('data',[]):
        if c['status'] in ['WAITING','ACTIVE']:request('/health/chat/'+str(c['id'])+'/close','POST',{},tok)
check('第一位患者申请问询',request('/health/chat/request','POST',{},p).get('code')==200)
check('第二位患者申请问询',request('/health/chat/request','POST',{},other).get('code')==200)
q=request('/health/chat/queue',token=doc)['data'];ids=[x['id'] for x in q if x['status']=='WAITING']
check('第二位患者显示等待人数',request('/health/chat/queue',token=other)['data'][0]['ahead']>=1)
check('不允许跳过排队接诊',request('/health/chat/'+str(ids[-1])+'/accept','POST',{},doc).get('code')!=200)
check('健康师按顺序接诊',request('/health/chat/'+str(ids[0])+'/accept','POST',{},doc).get('code')==200)
check('问询消息存入数据库',request('/health/chat/'+str(ids[0])+'/messages','POST',{'content':'课程消息留存测试'},p).get('code')==200)
check('第三方患者不能读取咨询',request('/health/chat/'+str(ids[0])+'/messages',token=other).get('code')!=200)
check('健康师不能同时接诊两人',request('/health/chat/'+str(ids[-1])+'/accept','POST',{},doc).get('code')!=200)
request('/health/chat/'+str(ids[0])+'/close','POST',{},doc)
check('结束后可接诊下一位',request('/health/chat/'+str(ids[-1])+'/accept','POST',{},doc).get('code')==200)
request('/health/chat/'+str(ids[-1])+'/close','POST',{},doc)
check('知识文章可读取',len(request('/health/chat/knowledge',token=p).get('data',[]))>=2)
for _ in range(20):
    pending=int(sql('SELECT COUNT(*) FROM t_event_outbox WHERE sent=0;'))
    missing=int(sql('SELECT COUNT(*) FROM t_event_outbox o LEFT JOIN t_event_receipt r ON r.id=o.id WHERE r.id IS NULL;'))
    if not pending and not missing:break
    time.sleep(.5)
check('RabbitMQ事件发布及幂等消费',pending==0 and missing==0,f'pending={pending},missing={missing}')
print(json.dumps({'total':len(results),'passed':sum(r['passed'] for r in results)},ensure_ascii=False))
raise SystemExit(0 if all(x['passed'] for x in results) else 1)
