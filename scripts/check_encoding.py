from pathlib import Path
import json,subprocess,urllib.request
R=Path(__file__).resolve().parents[1]
login=urllib.request.Request('http://127.0.0.1:18080/login',data=json.dumps({'username':'zhangsan','password':'admin123','code':'','uuid':''}).encode(),headers={'Content-Type':'application/json'})
token=json.load(urllib.request.urlopen(login))['token']
goods=json.load(urllib.request.urlopen(urllib.request.Request('http://127.0.0.1:18080/health/shop/goods',headers={'Authorization':'Bearer '+token})))['data']
q=subprocess.run(['docker','exec','-i','myhealthy-course-mysql-1','sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" mysql -N -B -umyhealthy myhealthy_course'],input='SET NAMES utf8mb4; SELECT TIMESTAMPDIFF(SECOND,UTC_TIMESTAMP(),NOW());',text=True,encoding='utf-8',capture_output=True,check=True).stdout.strip()
p=R/'docs/supplemental-results.json';a=json.loads(p.read_text(encoding='utf-8'));a=[x for x in a if x['name']not in ['初始化商品中文正确','数据库统一北京时间']]
a.extend([{'name':'初始化商品中文正确','passed':goods[0]['title']=='教学体检优惠券','detail':goods[0]['title']},{'name':'数据库统一北京时间','passed':q=='28800','detail':'UTC偏移秒数='+q}])
p.write_text(json.dumps(a,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(a[-2:],ensure_ascii=False))
if not all(x['passed'] for x in a[-2:]):raise SystemExit(1)
