function [flux,var,mask] = decorrelation(obs,eff,fpmask,fnmask)

feff = fft2(eff);
fobs = fft2(obs.*eff);

pnorm = real(ifftshift(ifft2(feff.*fpmask)));
nnorm = real(ifftshift(ifft2(feff.*fnmask)));
maskres = (pnorm>1).*(nnorm>1);

tmp1 = real(ifftshift(ifft2(fobs.*fpmask)))./pnorm .* maskres; 
tmp2 = real(ifftshift(ifft2(fobs.*fnmask)))./nnorm .* (nnorm>1);
flux = real((tmp1-tmp2) .* maskres);
flux(find(isnan(flux)))=0;

feff = fft2(eff.*eff);
fobs = fft2(obs.*(eff.*eff));
%pnorm = real(ifftshift(ifft2(feff.*fpmask)));
%nnorm = real(ifftshift(ifft2(feff.*fnmask)));
%maskres = (pnorm>1).*(nnorm>1);
tmp1 = real(ifftshift(ifft2(fobs.*fpmask)))./(pnorm.*pnorm) .* maskres; 
tmp2 = real(ifftshift(ifft2(fobs.*fnmask)))./(nnorm.*nnorm) .* (nnorm>1);
var = real((tmp1+tmp2)) +1e7*(1-maskres);
var(find(isnan(var)))=1e7;

mask=maskres;

%res =ifftshift(ifft2(fobs.*fnmask))./nnorm.*(nnorm>1);