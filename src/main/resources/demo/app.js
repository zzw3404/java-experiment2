"use strict";

const get = id => document.getElementById(id);
const fields = ["empName", "gender", "dept", "post", "salary", "hireDate", "status"];
const pager = { page: 1, size: 5, pages: 1 };
const selected = new Set();
const filters = get("filters");
const form = get("employee-form");
const editor = get("editor");
let editingId = null;

function notice(text = "", error = false, id = "message") {
    const element = get(id);
    element.textContent = text;
    element.hidden = !text;
    element.classList.toggle("error", error);
}

async function request(path, method = "GET", body, loginRequest = false) {
    const response = await fetch(new URL(".." + path, window.location.href), {
        method,
        headers: body === undefined ? {} : { "Content-Type": "application/json" },
        body: body === undefined ? undefined : JSON.stringify(body)
    });
    const data = await response.json();
    if (response.status === 401 && !loginRequest) showSession(false);
    if (!response.ok || data.code !== 200) throw new Error(data.message || "请求失败");
    return data.data;
}

function showSession(active) {
    get("console").hidden = !active;
    get("logout").hidden = !active;
    get("sign-in").hidden = active;
    if (!active && editor.open) editor.close();
}

function button(label, action) {
    const element = document.createElement("button");
    element.type = "button";
    element.textContent = label;
    element.addEventListener("click", () => run(action));
    return element;
}

function updateSelection() {
    const boxes = [...get("employee-rows").querySelectorAll("input")];
    get("batch").disabled = selected.size === 0;
    get("select-all").checked = boxes.length > 0 && boxes.every(box => box.checked);
    get("select-all").indeterminate = boxes.some(box => box.checked) && !get("select-all").checked;
}

function renderEmployees(records) {
    const tbody = get("employee-rows");
    tbody.replaceChildren();
    selected.clear();
    for (const row of records) {
        const tr = tbody.insertRow();
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.setAttribute("aria-label", "选择 " + row.empName);
        checkbox.addEventListener("change", () => {
            if (checkbox.checked) selected.add(row.empId);
            else selected.delete(row.empId);
            updateSelection();
        });
        tr.insertCell().append(checkbox);
        const values = [row.empId, row.empName, row.gender, row.dept, row.post,
            Number(row.salary).toLocaleString("zh-CN", {minimumFractionDigits: 2}), row.hireDate,
            row.status === 1 ? "在职" : "离职"];
        for (const value of values) tr.insertCell().textContent = value;
        tr.cells[8].className = row.status === 1 ? "active" : "inactive";
        tr.insertCell().append(button("查看", () => editEmployee(row.empId, true)),
            button("编辑", () => editEmployee(row.empId, false)),
            button("删除", () => removeEmployees([row.empId])));
    }
    if (!records.length) {
        const cell = tbody.insertRow().insertCell();
        cell.colSpan = 10;
        cell.textContent = "没有符合条件的员工";
    }
    updateSelection();
}

async function loadEmployees() {
    const query = new URLSearchParams({ page: pager.page, size: pager.size });
    for (const [key, value] of new FormData(filters)) {
        if (value.trim()) query.set(key, value.trim());
    }
    const data = await request("/emps?" + query);
    pager.pages = Math.max(1, data.pages);
    if (pager.page > pager.pages) {
        pager.page = pager.pages;
        return loadEmployees();
    }
    renderEmployees(data.records);
    get("page-info").textContent = "共 " + data.total + " 人 · 第 " + pager.page + " / " + pager.pages + " 页";
    get("prev").disabled = pager.page <= 1;
    get("next").disabled = pager.page >= pager.pages;
}

async function loadDepartments() {
    const data = await request("/emps/departments");
    const select = filters.elements.dept;
    const previous = select.value;
    select.replaceChildren(new Option("全部部门", ""));
    get("department-options").replaceChildren();
    get("department-grid").replaceChildren();
    for (const row of data) {
        select.add(new Option(row.dept, row.dept));
        get("department-options").append(new Option(row.dept, row.dept));
        const card = document.createElement("article");
        card.className = "department";
        const title = document.createElement("h3");
        title.textContent = row.dept;
        const counts = document.createElement("p");
        counts.textContent = "员工 " + row.total + " 人，在职 " + row.active + " 人";
        card.append(title, counts, button("查看员工", async () => {
            filters.reset();
            filters.elements.dept.value = row.dept;
            pager.page = 1;
            showTab(false);
            await loadEmployees();
        }));
        get("department-grid").append(card);
    }
    select.value = data.some(row => row.dept === previous) ? previous : "";
}

function showTab(departmentTab) {
    get("employees").hidden = departmentTab;
    get("departments").hidden = !departmentTab;
    get("show-employees").classList.toggle("primary", !departmentTab);
    get("show-departments").classList.toggle("primary", departmentTab);
}

async function editEmployee(id = null, readOnly = false) {
    const employee = id === null ? null : await request("/emps/" + id);
    editingId = id;
    form.reset();
    for (const field of fields) {
        if (employee) form.elements[field].value = employee[field];
        form.elements[field].disabled = readOnly;
    }
    get("editor-title").textContent = readOnly ? "员工档案详情" : id === null ? "新增员工" : "编辑员工";
    get("save").hidden = readOnly;
    notice("", false, "editor-message");
    editor.showModal();
}

async function removeEmployees(ids) {
    if (!window.confirm("确认删除选中的 " + ids.length + " 条员工档案？")) return;
    if (ids.length === 1) await request("/emps/" + ids[0], "DELETE");
    else await request("/emps", "DELETE", ids);
    notice("已删除 " + ids.length + " 条员工档案");
    await loadDepartments();
    await loadEmployees();
}

async function run(action, channel = "message") {
    try { await action(); }
    catch (error) { notice(error.message || "请求失败", true, channel); }
}

async function activate() {
    showSession(true);
    showTab(false);
    await loadDepartments();
    await loadEmployees();
}

get("login-form").addEventListener("submit", event => {
    event.preventDefault();
    const credentials = Object.fromEntries(new FormData(event.currentTarget));
    run(async () => {
        await request("/auth/login", "POST", credentials, true);
        get("login-form").elements.password.value = "";
        notice();
        await activate();
    });
});
get("logout").onclick = () => run(async () => {
    await request("/auth/logout", "POST");
    showSession(false);
    notice("已退出");
});
get("add").onclick = () => run(() => editEmployee());
get("batch").onclick = () => run(() => removeEmployees([...selected]));
get("close").onclick = () => editor.close();
get("prev").onclick = () => run(async () => { pager.page--; await loadEmployees(); });
get("next").onclick = () => run(async () => { pager.page++; await loadEmployees(); });
get("show-employees").onclick = () => showTab(false);
get("show-departments").onclick = () => run(async () => { await loadDepartments(); showTab(true); });
get("select-all").onchange = event => {
    const checked = event.currentTarget.checked;
    for (const box of get("employee-rows").querySelectorAll("input")) {
        box.checked = checked;
        box.dispatchEvent(new Event("change"));
    }
};
filters.onsubmit = event => {
    event.preventDefault();
    pager.page = 1;
    run(loadEmployees);
};
filters.onreset = () => {
    setTimeout(() => { pager.page = 1; run(loadEmployees); }, 0);
};
form.onsubmit = event => {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(form));
    values.salary = Number(values.salary);
    values.status = Number(values.status);
    if (editingId !== null) values.empId = editingId;
    run(async () => {
        await request("/emps", editingId === null ? "POST" : "PUT", values);
        const message = editingId === null ? "员工已新增" : "员工档案已更新";
        if (editingId === null) { filters.reset(); pager.page = 1; }
        editor.close();
        notice(message);
        await loadDepartments();
        await loadEmployees();
    }, "editor-message");
};
request("/auth/me", "GET", undefined, true).then(() => run(activate), () => showSession(false));

