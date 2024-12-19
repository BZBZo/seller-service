document.addEventListener('DOMContentLoaded', function() {
    // Quill 에디터 설정
    let quill = new Quill('#editor', {
        theme: 'snow',
        modules: {
            toolbar: [
                ['bold', 'italic', 'underline'],
                ['image']
            ]
        }
    });

    // 최대 파일 크기 설정 (2MB)
    const MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    // 에디터 초기화 시 기존 설명 로드
    quill.root.innerHTML = document.getElementById('description').textContent;

    // 폼 제출 시 Quill 내용 동기화 및 제출 처리
    function submitForm() {
        const description = quill.root.innerHTML;
        document.getElementById('description').value = description;

        const formData = new FormData(document.getElementById('productForm'));

        fetch(document.getElementById('productForm').action, {
            method: 'PUT',
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('네트워크 응답에 문제가 있습니다.');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    alert('상품이 성공적으로 수정되었습니다!');
                    window.location.href = '/seller/product/list';
                } else {
                    alert('상품 수정 중 오류가 발생했습니다.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('상품 수정 중 오류가 발생했습니다.');
            });
    }

    // 이미지 압축 및 미리보기 처리 함수
    window.previewMainImage = function(event) {
        const input = event.target;
        const imagePreview = document.getElementById('imagePreview');
        const placeholderText = document.getElementById('placeholderText');

        if (input.files && input.files[0]) {
            const file = input.files[0];

            // 파일 크기 확인
            if (file.size > MAX_FILE_SIZE) {
                alert('이미지 파일 크기가 너무 큽니다. 2MB 이하의 파일을 선택해 주세요.');
                return;
            }

            // FileReader로 이미지 로드
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = new Image();
                img.src = e.target.result;

                img.onload = function() {
                    const canvas = document.createElement('canvas');
                    const maxSize = 800; // 최대 가로/세로 크기 설정
                    let width = img.width;
                    let height = img.height;

                    // 이미지 크기 조정
                    if (width > height) {
                        if (width > maxSize) {
                            height *= maxSize / width;
                            width = maxSize;
                        }
                    } else {
                        if (height > maxSize) {
                            width *= maxSize / height;
                            height = maxSize;
                        }
                    }
                    canvas.width = width;
                    canvas.height = height;

                    // Canvas에 이미지 그리기
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(img, 0, 0, width, height);

                    // 압축된 이미지 데이터 URL 생성 (JPEG 형식, 80% 품질)
                    canvas.toBlob((blob) => {
                        const compressedFile = new File([blob], file.name, { type: "image/jpeg", lastModified: Date.now() });

                        // 압축된 이미지 미리보기
                        imagePreview.src = URL.createObjectURL(blob);
                        imagePreview.style.display = 'block';
                        placeholderText.style.display = 'none';

                        // 압축된 파일을 formData에 설정
                        const formData = new FormData(document.getElementById('productForm'));
                        formData.set('mainPicture', compressedFile);
                    }, "image/jpeg", 0.8);
                };
            };
            reader.readAsDataURL(file);
        } else {
            // 파일이 선택되지 않은 경우 미리보기를 초기화
            imagePreview.src = '';
            imagePreview.style.display = 'none';
            placeholderText.style.display = 'block';
        }
    };

    // 전역에서 submitForm 함수 접근 가능하도록 설정
    window.submitForm = submitForm;
});

function toggleFields(isVisible) {
    const conditionTable = document.querySelector('.condition-container');
    const addConditionBtn = document.getElementById('addConditionBtn');
    const conditionTableBody = document.querySelector('#conditionTable tbody');
    const conditionInput = document.querySelector('input[name="condition"]');

    // 방어 코드: 필요한 DOM 요소가 없으면 함수 종료
    if (!conditionTable || !addConditionBtn || !conditionTableBody || !conditionInput) {
        console.error("toggleFields: 필요한 DOM 요소가 없습니다.");
        return;
    }

    if (isVisible) {
        conditionTable.style.display = "block";
        addConditionBtn.style.display = "inline-block";

        if (conditionTableBody.children.length === 0) {
            addConditionRow(1, 0);
        }
    } else {
        conditionTable.style.display = "none";
        addConditionBtn.style.display = "none";
        conditionTableBody.innerHTML = "";
        conditionInput.value = "";
    }
}

function addConditionRow(people = 1, discount = 0) {
    const conditionTableBody = document.querySelector('#conditionTable tbody');
    const conditionInput = document.querySelector('input[name="condition"]');

    if (!conditionTableBody || !conditionInput) {
        console.error("addConditionRow: 필요한 DOM 요소가 없습니다.");
        return;
    }

    const row = document.createElement('tr');
    row.innerHTML = `
        <td><input type="number" class="people-input" value="${people}" min="1"></td>
        <td><input type="number" class="discount-input" value="${discount}" min="0"></td>
        <td><button type="button" class="remove-row">삭제</button></td>
    `;

    row.querySelector('.remove-row').addEventListener('click', () => {
        row.remove();
        updateCondition();
    });

    row.querySelectorAll('input').forEach(input => {
        input.addEventListener('input', updateCondition);
    });

    conditionTableBody.appendChild(row);
}

// 전역으로 선언
function updateCondition() {
    const conditionTableBody = document.querySelector('#conditionTable tbody');
    const conditionInput = document.querySelector('input[name="condition"]');

    if (!conditionTableBody || !conditionInput) {
        console.error("updateCondition: 필요한 DOM 요소가 없습니다.");
        return;
    }

    const rows = conditionTableBody.querySelectorAll('tr');
    const conditionArray = Array.from(rows).map(row => {
        const people = row.querySelector('.people-input').value || 1;
        const discount = row.querySelector('.discount-input').value || 0;
        return `{${people}:${discount}}`;
    });

    conditionInput.value = conditionArray.join(',');
    console.log('Updated condition:', conditionInput.value);
}

document.addEventListener('DOMContentLoaded', function () {
    const conditionInput = document.querySelector('input[name="condition"]');
    const addConditionBtn = document.getElementById('addConditionBtn');

    // 방어 코드: 필요한 DOM 요소가 없으면 함수 종료
    if (!conditionInput || !addConditionBtn) {
        console.error("초기화 과정에서 필요한 DOM 요소가 없습니다.");
        return;
    }

    // 기존 condition 값을 읽어와 테이블 초기화
    if (conditionInput.value.trim()) {
        conditionInput.value.split(',').forEach(cond => {
            const [people, discount] = cond.replace(/[{}]/g, '').split(':');
            addConditionRow(people, discount);
        });
    } else {
        addConditionRow(1, 0);
        updateCondition();
    }

    const isCongCheckedElement = document.querySelector('input[name="isCong"]:checked');
    const isCongChecked = isCongCheckedElement ? isCongCheckedElement.value === "true" : false;
    toggleFields(isCongChecked);

    addConditionBtn.addEventListener("click", () => {
        addConditionRow(1, 0);
        updateCondition();
    });
});

// 입력값 검증 함수
function validateInputs() {
    const conditionInput = document.querySelector('input[name="condition"]');
    const description = document.querySelector('#description').value.trim();

    if (!conditionInput.value.trim()) {
        alert("조건을 입력하세요.");
        console.error("조건 없음:", conditionInput.value);
        return false;
    }

    if (!description) {
        alert("설명을 입력하세요.");
        console.error("설명 없음:", description);
        return false;
    }

    return true;
}

// 폼 제출 함수
function submitForm() {
    if (!validateInputs()) {
        // 입력값이 유효하지 않은 경우 함수 종료
        return;
    }

    const formData = new FormData(document.getElementById('productForm'));

    fetch('/seller/product/update', {
        method: 'PUT',
        body: formData,
    })
        .then(response => {
            if (!response.ok) {
                console.error(`HTTP Error: ${response.status} - ${response.statusText}`);
                alert("서버 오류가 발생했습니다.");
                return;
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                alert('상품이 성공적으로 수정되었습니다!');
                window.location.href = '/seller/product/list';
            } else {
                console.error("서버 응답 실패:", data.message);
                alert('상품 수정 중 오류가 발생했습니다.');
            }
        })
        .catch(error => {
            console.error("Network Error:", error);
            alert('네트워크 오류가 발생했습니다.');
        });
}