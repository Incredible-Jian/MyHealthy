<template>
<el-container class="health-shell"><el-header><strong>慢病健康医疗</strong><el-space><el-tag>教学演示</el-tag><el-button @click="router.push('/index')">管理工作台</el-button><el-button @click="logout">退出</el-button></el-space></el-header>
<el-main><el-row :gutter="24"><el-col :span="24"><h1>让健康变化清晰可见</h1><p>测量、记录与随访，汇总在同一个健康档案中。模拟结果仅供课程演示。</p></el-col></el-row>
<el-row :gutter="20"><el-col v-for="item in indicators" :key="item.kind" :xs="24" :sm="12" :lg="6"><el-card shadow="hover"><p>{{item.name}}</p><h2>{{latest(item.kind)}}</h2><el-space><el-button type="primary" @click="router.push('/simulator/'+item.kind)">开始测量</el-button><el-button @click="router.push('/health-records/'+item.module)">记录</el-button></el-space></el-card></el-col></el-row>
<el-row :gutter="20" class="section"><el-col :xs="24" :lg="16"><el-card><template #header>最近10天的健康趋势</template><el-radio-group v-model="selected" @change="loadTrend"><el-radio-button v-for="i in indicators" :key="i.kind" :value="i.kind">{{i.name}}</el-radio-button></el-radio-group><section ref="chartEl" style="height:300px"></section><el-empty v-if="!trend.length" description="暂无测量记录" /></el-card></el-col><el-col :xs="24" :lg="8"><el-card><template #header>健康服务</template><el-space direction="vertical" fill><el-button v-for="m in modules" :key="m.module" @click="router.push('/health-records/'+m.module)">{{m.label}}</el-button><el-button @click="router.push('/simulator/sports')">运动手表</el-button><el-button type="primary" @click="generate">生成教学健康报告</el-button></el-space></el-card></el-col></el-row>
<el-card class="section"><template #header>异常提示</template><el-empty v-if="!data.alerts?.length" description="暂无提示"/><el-alert v-for="a in data.alerts" :key="a.id" :title="a.message" type="warning" :closable="false"/></el-card>
</el-main><el-footer>课程项目 · 本地模拟与真实接入状态分别记录</el-footer></el-container></template>
<script setup>
import {ref,onMounted,onBeforeUnmount,nextTick} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import * as echarts from 'echarts'
import {call} from '@/api/health'
import useUserStore from '@/store/modules/user'
const router=useRouter(),data=ref({}),selected=ref('pressure'),trend=ref([]),chartEl=ref();let chart
const indicators=[{kind:'pressure',module:'measurement',name:'血压'},{kind:'glucose',module:'myblood',name:'血糖'},{kind:'lipid',module:'lipid',name:'血脂'},{kind:'ecg',module:'heart',name:'心电图'}]
const modules=[{module:'tuser',label:'个人档案'},{module:'medichine',label:'用药记录'},{module:'record',label:'随访记录'},{module:'report',label:'健康报告'},{module:'device',label:'设备管理'},{module:'plan',label:'健康计划'},{module:'sports',label:'运动记录'}]
function latest(k){const d=data.value[k];if(!d)return '暂无记录';return k==='pressure'?`${d.systolic}/${d.diastolic} mmHg`:k==='glucose'?`${d.glucose} mmol/L`:k==='lipid'?`${d.total_cholesterol} mmol/L`:`${d.heart_rate} bpm`}
async function loadTrend(){try{trend.value=(await call('workflow/trends','get',null,{kind:selected.value})).data;await nextTick();chart??=echarts.init(chartEl.value);const field={pressure:'systolic',glucose:'glucose',lipid:'total_cholesterol',ecg:'heart_rate'}[selected.value];chart.setOption({tooltip:{trigger:'axis'},xAxis:{type:'category',data:trend.value.map(x=>x.measure_time)},yAxis:{type:'value'},series:[{type:'line',smooth:true,data:trend.value.map(x=>x[field]),areaStyle:{opacity:.1},lineStyle:{color:'#158b80'}}]},true)}catch(e){ElMessage.error('趋势加载失败，请稍后重试')}}
async function generate(){await call('workflow/report','post');ElMessage.success('已生成本地规则汇总报告，未调用AI');router.push('/health-records/report')}
async function logout(){await useUserStore().logOut();router.push('/login')}
function resize(){chart?.resize()}
onMounted(async()=>{data.value=(await call('workflow/dashboard')).data;await loadTrend();window.addEventListener('resize',resize)})
onBeforeUnmount(()=>{window.removeEventListener('resize',resize);chart?.dispose()})
</script>
<style scoped>.health-shell{min-height:100vh;background:#f1f7f6;color:#183c3a}.el-header{display:flex;align-items:center;justify-content:space-between;background:white;border-bottom:1px solid #dce9e7;padding:0 4vw}.el-main{padding:36px 4vw}h1{font-size:34px;margin:0 0 14px}h2{font-size:25px}.section{margin-top:24px}.el-card{border-radius:12px;margin-bottom:16px}.el-footer{text-align:center;color:#688681}.el-alert{margin-bottom:10px}</style>
