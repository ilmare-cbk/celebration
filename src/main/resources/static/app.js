const form = document.getElementById('create-form');
const occasionTypeEl = document.getElementById('occasionType');
const occasionNameWrap = document.getElementById('occasionNameWrap');
const imagesEl = document.getElementById('images');
const voiceEl = document.getElementById('voice');
const formError = document.getElementById('form-error');
const sizeHint = document.getElementById('size-hint');

const resultCard = document.getElementById('result-card');
const messageIdEl = document.getElementById('message-id');
const editTokenEl = document.getElementById('edit-token');
const publicLinkEl = document.getElementById('public-link');
const expiresAtEl = document.getElementById('expires-at');

const loadBtn = document.getElementById('load-public');
const publicTokenEl = document.getElementById('public-token');
const publicError = document.getElementById('public-error');
const previewEl = document.getElementById('preview');
const previewTitle = document.getElementById('preview-title');
const previewContent = document.getElementById('preview-content');
const previewOccasion = document.getElementById('preview-occasion');
const previewImages = document.getElementById('preview-images');
const previewVoice = document.getElementById('preview-voice');

const MB = 1024 * 1024;

occasionTypeEl.addEventListener('change', () => {
  occasionNameWrap.classList.toggle('hidden', occasionTypeEl.value !== 'CUSTOM');
});

function validateUploadSize() {
  const images = [...imagesEl.files];
  const voice = voiceEl.files[0];

  if (images.length > 3) {
    return '이미지는 최대 3장까지 업로드할 수 있습니다.';
  }

  const imageTooLarge = images.find(file => file.size > 3 * MB);
  if (imageTooLarge) {
    return `이미지 파일(${imageTooLarge.name})이 3MB를 초과합니다.`;
  }

  if (voice && voice.size > 5 * MB) {
    return '음성 파일은 5MB 이하여야 합니다.';
  }

  const totalSize = images.reduce((sum, file) => sum + file.size, 0) + (voice?.size || 0);
  sizeHint.textContent = `현재 총 업로드: ${(totalSize / MB).toFixed(2)}MB / 14MB`;
  if (totalSize > 14 * MB) {
    return '총 업로드 용량이 14MB를 초과했습니다.';
  }

  return null;
}

imagesEl.addEventListener('change', () => {
  formError.textContent = validateUploadSize() || '';
});
voiceEl.addEventListener('change', () => {
  formError.textContent = validateUploadSize() || '';
});

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  formError.textContent = '';

  const uploadError = validateUploadSize();
  if (uploadError) {
    formError.textContent = uploadError;
    return;
  }

  const data = new FormData(form);
  data.delete('images');
  [...imagesEl.files].forEach(file => data.append('images', file));
  if (!voiceEl.files[0]) {
    data.delete('voice');
  }

  try {
    const response = await fetch('/api/v1/messages', { method: 'POST', body: data });
    const body = await response.json();
    if (!response.ok) {
      throw new Error(body.message || '메시지 생성 실패');
    }

    const publicLink = `${window.location.origin}/?publicToken=${body.publicToken}`;
    messageIdEl.textContent = body.messageId;
    editTokenEl.textContent = body.editToken;
    publicLinkEl.textContent = publicLink;
    publicLinkEl.href = publicLink;
    expiresAtEl.textContent = new Date(body.expiresAt).toLocaleString();
    resultCard.classList.remove('hidden');
  } catch (error) {
    formError.textContent = error.message;
  }
});

function applyTemplateClass(templateCode) {
  previewEl.classList.remove('template-a', 'template-b', 'template-c');
  if (templateCode === 'TEMPLATE_A') previewEl.classList.add('template-a');
  if (templateCode === 'TEMPLATE_B') previewEl.classList.add('template-b');
  if (templateCode === 'TEMPLATE_C') previewEl.classList.add('template-c');
}

async function loadPublicMessage() {
  const token = publicTokenEl.value.trim();
  if (!token) {
    publicError.textContent = 'public token을 입력하세요.';
    return;
  }

  publicError.textContent = '';
  try {
    const response = await fetch(`/api/v1/messages/public/${token}`);
    const body = await response.json();
    if (!response.ok) {
      throw new Error(body.message || body.code || '조회 실패');
    }

    applyTemplateClass(body.templateCode);
    previewTitle.textContent = body.title;
    previewContent.textContent = body.content;
    previewOccasion.textContent = `${body.occasion.type}${body.occasion.name ? ` (${body.occasion.name})` : ''}`;

    previewImages.innerHTML = '';
    body.media.images.forEach(image => {
      const img = document.createElement('img');
      img.src = image.url;
      img.alt = `image-${image.sortOrder}`;
      previewImages.appendChild(img);
    });

    if (body.media.voice?.url) {
      previewVoice.src = body.media.voice.url;
      previewVoice.classList.remove('hidden');
    } else {
      previewVoice.src = '';
      previewVoice.classList.add('hidden');
    }

    previewEl.classList.remove('hidden');
  } catch (error) {
    previewEl.classList.add('hidden');
    publicError.textContent = error.message;
  }
}

loadBtn.addEventListener('click', loadPublicMessage);

const params = new URLSearchParams(window.location.search);
const prefilledToken = params.get('publicToken');
if (prefilledToken) {
  publicTokenEl.value = prefilledToken;
  loadPublicMessage();
}
