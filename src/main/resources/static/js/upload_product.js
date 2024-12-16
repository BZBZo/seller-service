let quill;

function previewMainImage(event) {
    const input = event.target;
    const imagePreview = document.getElementById('imagePreview');
    const placeholderText = document.getElementById('placeholderText');

    const file = input.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            imagePreview.src = e.target.result;
            imagePreview.style.display = 'block';
            placeholderText.style.display = 'none';
        };
        reader.readAsDataURL(file);
    } else {
        imagePreview.src = '';
        imagePreview.style.display = 'none';
        placeholderText.style.display = 'block';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    quill = new Quill('#editor', {
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

    // 이미지 URL이 유효한 경우 Quill 에디터에 삽입
    const insertImage = (imageSrc) => {
        if (!imageSrc) {
            console.error('유효하지 않은 이미지 URL입니다.');
            alert('이미지 URL이 유효하지 않아 미리보기를 표시할 수 없습니다.');
            return;
        }
        const range = quill.getSelection();
        if (range) {
            quill.insertEmbed(range.index, 'image', imageSrc);
            console.log('Image inserted into editor:', imageSrc);
        }
    };

    const handleImageUpload = (file) => {
        // 파일 크기 확인
        if (file.size > MAX_FILE_SIZE) {
            alert('이미지 파일 크기가 너무 큽니다. 2MB 이하의 파일을 선택해 주세요.');
            return;
        }

        // 이미지를 압축하고 압축된 파일로 업로드
        compressImage(file, (compressedFile) => {
            const formData = new FormData();
            formData.append('file', compressedFile);

            $.ajax({
                type: 'POST',
                url: '/seller/product/uploadImage',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    console.log('Upload response:', response);
                    if (response.imageUrl) {
                        const absoluteImageUrl = location.origin + response.imageUrl;
                        console.log('Absolute image URL:', absoluteImageUrl);
                        insertImage(absoluteImageUrl);
                        alert('이미지가 성공적으로 업로드되었습니다.');
                    } else {
                        alert('이미지 URL을 받지 못했습니다.');
                    }
                },
                error: function(xhr, status, error) {
                    console.error('업로드 중 오류 발생:', error);
                    alert('상품 등록 중 오류가 발생했습니다.');
                }
            });
        });
    };

    // 이미지 압축 함수
    const compressImage = (file, callback) => {
        const reader = new FileReader();
        reader.onload = function(event) {
            const img = new Image();
            img.src = event.target.result;

            img.onload = function() {
                const canvas = document.createElement('canvas');
                const maxSize = 800;
                let width = img.width;
                let height = img.height;

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

                const ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0, width, height);

                canvas.toBlob((blob) => {
                    const compressedFile = new File([blob], file.name, { type: "image/jpeg", lastModified: Date.now() });
                    callback(compressedFile);
                }, "image/jpeg", 0.8);
            };
        };
        reader.readAsDataURL(file);
    };

    quill.getModule('toolbar').addHandler('image', function() {
        const input = document.createElement('input');
        input.setAttribute('type', 'file');
        input.setAttribute('accept', 'image/*');
        input.click();
        input.onchange = () => {
            const file = input.files[0];
            if (file) {
                handleImageUpload(file);
            }
        };
    });

    // 폼 제출 로직
    document.addEventListener('DOMContentLoaded', function() {
        const productForm = document.getElementById('productForm');
        if (productForm) {
            productForm.onsubmit = function(event) {
                event.preventDefault(); // 기본 폼 제출 방지
                submitForm(); // submitForm 함수 호출
            };
        }
    });

    // 메인 이미지 미리보기 및 압축 설정
    const mainPictureInput = document.getElementById('mainPicture');
    if (mainPictureInput) {
        mainPictureInput.addEventListener('change', function(event) {
            const input = event.target;
            const imagePreview = document.getElementById('imagePreview');
            const placeholderText = document.getElementById('placeholderText');

            const file = input.files[0];
            if (file) {
                // 파일 크기 확인
                if (file.size > MAX_FILE_SIZE) {
                    alert('메인 이미지 파일 크기가 너무 큽니다. 2MB 이하의 파일을 선택해 주세요.');
                    return;
                }

                // 압축 및 미리보기 설정
                compressImage(file, (compressedFile) => {
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        imagePreview.src = e.target.result;
                        imagePreview.style.display = 'block';
                        placeholderText.style.display = 'none';
                    };
                    reader.readAsDataURL(compressedFile);
                });
            } else {
                imagePreview.src = '';
                imagePreview.style.display = 'none';
                placeholderText.style.display = 'block';
            }
        });
    }
});


document.addEventListener('DOMContentLoaded', function () {
    const conditionInput = document.querySelector('input[name="condition"]');
    const isCongInput = document.querySelector('input[name="isCong"]:checked');
    const peopleInput = document.querySelector('input[name="people"]');
    const discountInput = document.querySelector('input[name="discount"]');
    const extraFields = document.getElementById("extraFields");

    const initializeCondition = () => {
        const people = Math.max(parseInt(peopleInput?.value || 1, 10), 1); // 기본값 1, 최소값 검증
        const discount = Math.max(parseInt(discountInput?.value || 0, 10), 0); // 기본값 0, 최소값 검증
        conditionInput.value = `{${people}:${discount}}`;
        console.log('Condition initialized:', conditionInput.value);
    };

    if (isCongInput && isCongInput.value === "true") {
        extraFields.classList.remove("hidden");
        initializeCondition();
    } else {
        extraFields.classList.add("hidden");
        if (conditionInput) conditionInput.value = `{0:0}`;
    }

    // Add Event Listeners for input changes
    if (peopleInput && discountInput) {
        const updateCondition = () => {
            const people = Math.max(parseInt(peopleInput?.value || 1, 10), 1);
            const discount = Math.max(parseInt(discountInput?.value || 0, 10), 0);
            conditionInput.value = `{${people}:${discount}}`;
            console.log('Updated Condition:', conditionInput.value);
        };

        peopleInput.addEventListener('input', updateCondition);
        discountInput.addEventListener('input', updateCondition);
    }
});

function toggleFields(isVisible, element) {
    const extraFields = document.getElementById("extraFields");
    const peopleInput = document.querySelector('input[name="people"]');
    const discountInput = document.querySelector('input[name="discount"]');

    console.log(`Selected Cong State: ${element.value}, Visible: ${isVisible}`);

    if (isVisible) {
        extraFields.classList.remove("hidden");
        peopleInput.value = '1'; // 최소값 설정
        discountInput.value = '0';
    } else {
        extraFields.classList.add("hidden");
        peopleInput.value = '';
        discountInput.value = '';
    }
}

function submitForm() {
    const productForm = document.getElementById('productForm');
    const formData = new FormData(productForm);
    const conditionField = document.getElementById('condition');
    const peopleInput = document.getElementById('people');
    const discountInput = document.getElementById('discount');

    // Quill 에디터 내용 동기화
    const descriptionValue = quill.root.innerHTML;
    formData.set('description', descriptionValue); // FormData에 강제로 추가
    console.log('Description value:', descriptionValue);

    // Condition 필드 업데이트
    const updateConditionField = (peopleInput, discountInput, conditionField) => {
        const people = Math.max(parseInt(peopleInput?.value || 1, 10), 1);
        const discount = Math.max(parseInt(discountInput?.value || 0, 10), 0);
        conditionField.value = `{${people}:${discount}}`;
    };

    updateConditionField(peopleInput, discountInput, conditionField);

    console.log('Condition:', conditionField.value);
    console.log(...formData.entries()); // FormData 확인

    // 서버에 데이터 전송
    fetch('/seller/product', {
        method: 'POST',
        body: formData,
    })
        .then((response) => {
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return response.json();
        })
        .then((data) => {
            if (data.url) {
                alert('상품이 성공적으로 등록되었습니다!');
                window.location.href = data.url;
            } else {
                alert('응답 URL이 없습니다.');
            }
        })
        .catch((error) => {
            console.error('Error occurred during product registration:', error);
            alert('상품 등록 중 문제가 발생했습니다. 관리자에게 문의하세요.');
        });
}