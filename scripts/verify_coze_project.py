"""Authorized end-to-end check using only course synthetic accounts and inputs."""
from pathlib import Path
import urllib.request,urllib.error,json,time,re,datetime
R=Path(__file__).resolve().parents[1];results=[];answers={};base='http://127.0.0.1:18080'
def req(path,method='GET',body=None,token=None):
 h={'Content-Type':'application/json'}
 if token:h['Authorization']='Bearer '+token
 request=urllib.request.Request(base+path,data=None if body is None else json.dumps(body,ensure_ascii=False).encode(),headers=h,method=method)
 try:
  with urllib.request.urlopen(request,timeout=75)as f:return json.load(f)
 except urllib.error.HTTPError as e:
  try:return json.load(e)
  except:return {'code':e.code}
def check(name,passed,detail=''):
 results.append({'name':name,'passed':bool(passed),'detail':detail});print(('PASS 'if passed else'FAIL ')+name,flush=True)
 (R/'docs/coze-project-results.json').write_text(json.dumps({'executedAt':datetime.datetime.now().isoformat(timespec='seconds'),'results':results,'teachingAnswers':answers},ensure_ascii=False,indent=2),encoding='utf-8')
for i in range(80):
 try:
  if req('/captchaImage').get('code')==200:break
 except Exception:time.sleep(1)
else:raise RuntimeError('Local backend not ready')
def login(name):return req('/login','POST',{'username':name,'password':'admin123','code':'','uuid':''})['token']
p=login('zhangsan');teacher=login('healthteacher');other=login('lisi')
check('项目读取Coze启用状态',req('/health/ai/status',token=p).get('data',{}).get('configured')is True)
r=req('/health/ai/consult','POST',{'question':'课程模拟测量是否可以作为真实体检？请说明限制并给两个相关追问。'},p);d=r.get('data')or{};answers['consultation']=d.get('answer','')
check('患者咨询取得真实Coze回答',r.get('code')==200 and d.get('source')=='coze' and len(d.get('answer',''))>10)
check('模型追问可供页面继续提问',len(d.get('followups')or[])>=2,'追问数量='+str(len(d.get('followups')or[])))
for kind,sample,expected in [('pressure','pressure-001',['105','65','60']),('glucose','glucose-001',['4.0']),('lipid','lipid-001',['3.8','1.0','1.3','2.5']),('ecg','ecg-001',['60'])]:
 r=req('/health/ai/measurement/'+kind,'POST',{'age':20,'heightCm':160,'weightKg':50,'mealState':'fasting'},p);d=r.get('data')or{};answer=d.get('answer','');answers[kind]=answer
 check(kind+'已知教学样本返回一致',r.get('code')==200 and d.get('source')=='coze' and sample in answer and all(x in answer for x in expected))
r=req('/health/ai/measurement/pressure','POST',{'age':'not-a-number','heightCm':160,'weightKg':50},p)
check('AI测量入口拒绝非法参数',r.get('code')!=200)
r=req('/health/ai/measurement/pressure','POST',{'age':117,'heightCm':249,'weightKg':349},p);d=r.get('data')or{};answer=d.get('answer','');answers['unknownSample']=answer
check('未知样本明确匹配限制',r.get('code')==200 and any(word in answer for word in ['未匹配','未找到','没有','无精确','不完全','近似','无法','不匹配','无匹配']))
before=req('/health/workflow/reports',token=p).get('data')or[];previous={x['id']for x in before}
r=req('/health/ai/report','POST',{},p);d=r.get('data')or{};answers['report']=d.get('answer','')
check('项目生成真实AI报告',r.get('code')==200 and d.get('source')=='coze' and bool(d.get('answer')))
rows=req('/health/workflow/reports',token=p).get('data')or[];new=[x for x in rows if x['id']not in previous and x.get('source')=='coze']
check('AI报告保存为Coze来源草稿',bool(new)and new[0].get('status')=='draft')
if new:
 row=new[0];rid=row['id'];content=row.get('content') or answers['report']
 check('患者不能自行审核AI报告',req('/health/workflow/report/'+str(rid)+'/review','POST',{'content':content},p).get('code')!=200)
 check('其他患者不能读取该AI报告',req('/health/report/'+str(rid),token=other).get('code')!=200)
 review=req('/health/workflow/report/'+str(rid)+'/review','POST',{'content':content+'\n\n健康师教学审核：已核对模拟来源，报告不用于医学诊断。'},teacher)
 rows=req('/health/workflow/reports',token=p).get('data')or[];saved=next((x for x in rows if x['id']==rid),{})
 check('健康师审核AI报告并保留来源',review.get('code')==200 and saved.get('status')=='approved' and saved.get('source')=='coze')
print(json.dumps({'total':len(results),'passed':sum(x['passed']for x in results)},ensure_ascii=False))
