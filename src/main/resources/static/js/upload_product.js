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


document.addEventListener('DOMContentLoaded', function() {
    const conditionInput = document.querySelector('input[name="condition"]');
    const isCongInput = document.querySelector('input[name="isCong"]:checked');
    const peopleInput = document.querySelector('input[name="people"]');
    const discountInput = document.querySelector('input[name="discount"]');
    const extraFields = document.getElementById("extraFields");

    if (isCongInput && isCongInput.value === "true") {
        extraFields.classList.remove("hidden");
        if (conditionInput && peopleInput && discountInput) {
            const people = peopleInput.value || 0;
            const discount = discountInput.value || 0;
            conditionInput.value = `{${people}:${discount}}`;
            console.log('Initial Condition:', conditionInput.value);
        }
    } else {
        extraFields.classList.add("hidden");
        if (conditionInput) {
            conditionInput.value = `{0:0}`;
        }
    }

    // Add Event Listeners for input changes
    if (peopleInput && discountInput) {
        const updateCondition = () => {
            if (conditionInput) {
                const people = peopleInput.value || 0;
                const discount = discountInput.value || 0;
                conditionInput.value = `{${people}:${discount}}`;
                console.log('Updated Condition:', conditionInput.value);
            }
        };

        peopleInput.addEventListener('input', updateCondition);
        discountInput.addEventListener('input', updateCondition);
    }
});

function toggleFields(isVisible, element) {
    const extraFields = document.getElementById("extraFields");
    const peopleInput = document.querySelector('input[name="people"]');
    const discountInput = document.querySelector('input[name="discount"]');

    // 로그 출력
    console.log(`선택한 공구 진행 상태: ${element.value}`); // HTML 요소 값
    console.log(`공구 진행 상태: ${isVisible ? "가능" : "불가"}`); // 논리 상태

    if (isVisible) {
        // 보이기 (값 초기화)
        if (peopleInput) peopleInput.value = '0'; // 모집인원 초기화
        if (discountInput) discountInput.value = '0'; // 할인율 초기화
        extraFields.classList.remove("hidden");
    } else {
        // 숨기기
        extraFields.classList.add("hidden");
    }
}

function submitForm() {

    const productForm = document.getElementById('productForm');
    const formData = new FormData(productForm);
    const peopleInput = document.getElementById('people');
    const discountInput = document.getElementById('discount');
    const conditionField = document.getElementById('condition');

    // 모집인원과 할인율을 조합하여 condition 필드에 저장
    const people = peopleInput?.value || 0; // 값이 없으면 기본값 0
    const discount = discountInput?.value || 0; // 값이 없으면 기본값 0
    conditionField.value = `{${people}:${discount}}`;

    console.log('People:', peopleInput ? peopleInput.value : "없음");
    console.log('Discount:', discountInput ? discountInput.value : "없음");
    console.log('Condition:', conditionField.value);

    // description 추가
    const description = document.querySelector('#editor .ql-editor').innerHTML;
    formData.append('description', description);

    // 서버로 데이터 전송
    fetch('/seller/product', {
        method: 'POST',
        body: formData,
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then((data) => {
            if (data.url) {
                alert('상품이 성공적으로 등록되었습니다!');
                console.log('서버 응답:', data);
                console.log('서버에서 반환된 URL:', data.url);
                window.location.href = data.url; // 서버가 제공한 URL로 이동 리스트 페이지
            } else {
                alert('응답 URL이 없습니다.');
            }
        })
        .catch((error) => {
            console.error('상품 등록 중 오류 발생:', error);
            alert('상품 등록 중 문제가 발생했습니다. 다시 시도해주세요.');
            window.location.href = '/seller/product/upload';
        });
}
