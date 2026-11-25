const by=id=>document.getElementById(id);
const formData=form=>Object.fromEntries(new FormData(form));
async function send(method,path,data,show=true){
  const res=await fetch(new URL('..'+path, window.location.href),{method,headers:data===undefined?{}:{'Content-Type':'application/json'},body:data===undefined?undefined:JSON.stringify(data)});
  const json=await res.json();
  if(show){
    by('endpoint').textContent=method+' '+path;
    by('status').textContent='HTTP '+res.status+' · '+json.message;
    by('response').textContent=JSON.stringify(json,null,2);
  }
  return {status:res.status,json};
}
function logged(value){
  by('loginPanel').hidden=Boolean(value);by('nav').hidden=!value;
  view(value?'dashboard':null);
  by('session').textContent=value?value+' · 已登录':'未登录';
}
async function list(filters={},show=true){
  const params=new URLSearchParams(Object.entries(filters).filter(([,v])=>v!==''));
  const {json}=await send('GET','/products'+(params.size?'?'+params:''),undefined,show);
  if(!json.data)return;
  const rows=json.data.records;by('total').textContent='共 '+json.data.total+' 条 · 当前显示 '+rows.length+' 条';
  by('products').replaceChildren(...rows.map(p=>{
    const tr=document.createElement('tr');
    const checkbox=document.createElement('input');checkbox.type='checkbox';checkbox.value=p.id;
    checkbox.setAttribute('aria-label','选择 '+p.productName);tr.insertCell().append(checkbox);
    for(const value of [p.id,p.productName,p.category,p.brand,p.price,p.stock,p.saleStatus==='ON_SALE'?'在售':'下架']){
      const td=tr.insertCell();td.textContent=value;
    }
    const actions=tr.insertCell(), details=document.createElement('button'),del=document.createElement('button');
    details.textContent='详情';details.className='secondary';details.onclick=()=>send('GET','/products/'+p.id);
    del.textContent='删除';del.className='secondary';del.onclick=async()=>{if(!confirm('删除商品 '+p.productName+'？'))return;const a=await send('DELETE','/products/'+p.id);if(a.status===200)await list({},false);};
    const edit=document.createElement('button');
    edit.textContent='编辑';edit.className='secondary';edit.onclick=()=>startEdit(p);
    actions.append(details,' ',edit,' ',del);return tr;
  }));
}
by('probe').onclick=()=>send('GET','/products');
by('login').onsubmit=async e=>{e.preventDefault();const result=await send('POST','/auth/login',formData(e.target));if(result.status===200){logged(result.json.data);e.target.reset();await list({},false);}};
by('logout').onclick=async()=>{await send('POST','/auth/logout');logged(false);};
by('filter').onsubmit=e=>{e.preventDefault();return list(formData(e.target));};
by('all').onclick=()=>{by('filter').reset();return list();};
function view(name){
  for(const id of ['dashboard','brands','orders','lines'])by(id).hidden=id!==name;
  by('editor').hidden=name!=='dashboard';
  by('nav').querySelectorAll('[data-view]').forEach(button=>{
    if(button.dataset.view===name)button.setAttribute('aria-current','page');
    else button.removeAttribute('aria-current');
  });
}
function rows(target,data,fields,action){
  by(target).replaceChildren(...data.map(row=>{
    const tr=document.createElement('tr');
    for(const key of fields)tr.insertCell().textContent=row[key]??'—';
    if(action)tr.insertCell().append(action(row));
    return tr;
  }));
}
async function openView(name){
  view(name);
  if(name==='brands'){
    const r=await send('GET','/catalog/brands');
    rows('brandRows',r.json.data||[],['id','brandName','website','productCount']);
  }else if(name==='orders'){
    const r=await send('GET','/catalog/orders');
    rows('orderRows',r.json.data||[],['orderNo','customerName','orderedOn','orderStatus'],order=>{
      const button=document.createElement('button');button.textContent='看明细';
      button.onclick=()=>{by('lineQuery').elements.id.value=order.id;return openView('lines');};
      return button;
    });
  }else if(name==='lines'){
    const id=by('lineQuery').elements.id.value;
    const r=await send('GET','/catalog/orders/'+id+'/lines');
    rows('lineRows',r.json.data||[],['productId','productName','quantity','dealPrice','subtotal']);
  }else await list();
}
by('nav').querySelectorAll('[data-view]').forEach(button=>button.onclick=()=>openView(button.dataset.view));
by('lineQuery').onsubmit=e=>{e.preventDefault();return openView('lines');};
by('batch').onclick=async()=>{
  const ids=[...by('products').querySelectorAll('input:checked')].map(x=>Number(x.value));
  if(!ids.length){by('status').textContent='请先勾选商品';return;}
  if(!confirm('删除选中的 '+ids.length+' 件商品？'))return;
  const r=await send('DELETE','/products',ids);
  if(r.status===200)await list({},false);
};
function startEdit(product){
  const f=by('save');
  for(const field of ['id','productName','category','brandId','price','stock','saleStatus'])f.elements[field].value=product[field];
  by('editorTitle').textContent='编辑商品 #'+product.id;
  by('saveBtn').textContent='保存修改';by('resetEditor').hidden=false;
}
function resetEditor(){
  by('save').reset();by('editorTitle').textContent='新增商品';
  by('saveBtn').textContent='新增商品';by('resetEditor').hidden=true;
}
by('resetEditor').onclick=resetEditor;
by('save').onsubmit=async e=>{
  e.preventDefault();const data=formData(e.target);
  for(const k of ['brandId','stock','price'])data[k]=Number(data[k]);
  const method=data.id?'PUT':'POST';
  if(data.id)data.id=Number(data.id);else delete data.id;
  const r=await send(method,'/products',data);
  if(r.status===200){resetEditor();await list({},false);}
};
