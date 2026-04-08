const form = document.getElementById("studentForm");
const formTitle = document.getElementById("formTitle");
const studentIdInput = document.getElementById("studentId");
const enrollmentNoInput = document.getElementById("enrollmentNo");
const nameInput = document.getElementById("name");
const ageInput = document.getElementById("age");
const departmentInput = document.getElementById("department");
const cgpaInput = document.getElementById("cgpa");
const tableWrap = document.getElementById("tableWrap");
const searchBox = document.getElementById("searchBox");
const cancelBtn = document.getElementById("cancelBtn");

let students = [];

async function fetchStudents() {
    try {
        const response = await fetch("/api/students");
        if (!response.ok) {
            throw new Error("Could not load students");
        }
        students = await response.json();
        renderTable();
    } catch (error) {
        tableWrap.innerHTML = "<p>Could not load student records. Please refresh the page.</p>";
    }
}

function renderTable() {
    const term = searchBox.value.trim().toLowerCase();
    const filtered = students.filter((s) =>
        s.name.toLowerCase().includes(term) ||
        s.department.toLowerCase().includes(term) ||
        String(s.enrollmentNo).includes(term)
    );

    const rows = filtered.length
        ? filtered.map((s) => `
        <tr>
            <td>${s.id}</td>
            <td>${escapeHtml(s.name)}</td>
            <td>${s.age}</td>
            <td>${escapeHtml(s.enrollmentNo)}</td>
            <td>${escapeHtml(s.department)}</td>
            <td>${Number(s.cgpa).toFixed(2)}</td>
            <td>
                <div class="row-actions">
                    <button class="small secondary" onclick="startEdit(${s.id})">Edit</button>
                    <button class="small" onclick="deleteStudent(${s.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join("")
        : `
        <tr>
            <td colspan="7">No students found.</td>
        </tr>
    `;

    tableWrap.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Age</th>
                    <th>Enrollment No.</th>
                    <th>Department</th>
                    <th>CGPA</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>${rows}</tbody>
        </table>
    `;
}

function formDataFromInputs() {
    const data = new URLSearchParams();
    data.append("name", nameInput.value.trim());
    data.append("age", ageInput.value.trim());
    data.append("enrollmentNo", enrollmentNoInput.value.trim());
    data.append("department", departmentInput.value.trim());
    data.append("cgpa", cgpaInput.value.trim());
    return data;
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const id = studentIdInput.value;
    const method = id ? "PUT" : "POST";
    const url = id ? `/api/students/${id}` : "/api/students";

    const response = await fetch(url, {
        method,
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: formDataFromInputs().toString(),
    });

    if (!response.ok) {
        const error = await response.json();
        alert(error.error || "Could not save student");
        return;
    }

    resetForm();
    await fetchStudents();
});

async function deleteStudent(id) {
    const ok = confirm("Delete this student record?");
    if (!ok) {
        return;
    }
    const response = await fetch(`/api/students/${id}`, { method: "DELETE" });
    if (!response.ok) {
        alert("Could not delete student");
        return;
    }
    await fetchStudents();
}

function startEdit(id) {
    const student = students.find((s) => s.id === id);
    if (!student) {
        return;
    }
    formTitle.textContent = "Edit Student";
    cancelBtn.hidden = false;
    studentIdInput.value = student.id;
    enrollmentNoInput.value = student.enrollmentNo;
    nameInput.value = student.name;
    ageInput.value = student.age;
    departmentInput.value = student.department;
    cgpaInput.value = student.cgpa;
}

function resetForm() {
    form.reset();
    formTitle.textContent = "Add Student";
    cancelBtn.hidden = true;
    studentIdInput.value = "";
}

cancelBtn.addEventListener("click", resetForm);
searchBox.addEventListener("input", renderTable);

function escapeHtml(value) {
    return value
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

window.startEdit = startEdit;
window.deleteStudent = deleteStudent;

tableWrap.innerHTML = "<p>Loading records...</p>";
fetchStudents();
